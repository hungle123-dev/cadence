package com.cadence.catalog.infrastructure.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.PriceBand;
import com.cadence.catalog.application.dto.PriceBands;
import com.cadence.catalog.application.dto.ProductFacetsView;
import com.cadence.catalog.application.dto.ProductFacetsView.FacetCount;
import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.catalog.application.port.driven.ProductListReadPort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.ProductImage;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.catalog.domain.valueobject.ProductPricing;
import com.cadence.catalog.domain.valueobject.ProductPublication;
import com.cadence.catalog.domain.valueobject.ProductStatus;
import com.cadence.catalog.domain.valueobject.Sale;
import com.cadence.common.application.PageResult;

import lombok.RequiredArgsConstructor;

/**
 * PLP and filter panel with native SQL (database doc 4.1). Filtering, sorting, paging and counting
 * happen in PostgreSQL, so the visibility, price and stock rules are written here a second time
 * (CatalogSql); every displayed value still comes from the domain.
 *
 * <p>Facets (FR-CAT-02) are counted per filter group with every chosen filter applied except the
 * group's own, so a count tells what choosing that value would add. One query per group: simple to
 * read and to test; measure with k6 before making it clever (M1 risk note).
 */
@Component
@RequiredArgsConstructor
public class ProductListJdbcAdapter implements ProductListReadPort {

    /** A filter group, so one group can be left out when its own facet is counted. */
    private enum Group {
        CATEGORY,
        GENDER,
        SPORT,
        COLOR,
        SIZE,
        PRICE,
        ON_SALE,
        /** Never left out: the category page narrows every count. */
        FIXED
    }

    private static final String TILES =
            """
            SELECT p.slug, p.name, p.status, p.launch_at,
                   p.list_price, p.sale_price, p.sale_starts_at, p.sale_ends_at,
                   (SELECT count(*) FROM catalog.colorways c2 WHERE c2.product_id = p.id) AS color_count,
                   EXISTS (SELECT 1 FROM catalog.variants v2 WHERE v2.product_id = p.id AND v2.available) AS in_stock,
                   i.image_key, i.width, i.height, i.alt
            FROM catalog.products p
            LEFT JOIN LATERAL (
                SELECT pi.image_key, pi.width, pi.height, pi.alt
                FROM catalog.colorways c
                JOIN catalog.product_images pi ON pi.colorway_id = c.id
                WHERE c.product_id = p.id
                ORDER BY c.sort_order, c.id, pi.sort_order, pi.id
                LIMIT 1
            ) i ON true
            """;

    private final JdbcClient jdbc;

    // Spring applies an isolation level only when it opens the transaction. Called through
    // ProductQueryService, whose transaction is already open, the service asks for REPEATABLE READ
    // itself; the setting here covers a direct call (the adapter tests).

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResult<ProductTileView> findTiles(ProductListQuery query, Instant now) {
        Where where = where(query, now, null);

        long total =
                jdbc.sql("SELECT count(*) FROM catalog.products p WHERE " + where.sql())
                        .params(where.params())
                        .query(Long.class)
                        .single();

        List<ProductTileView> tiles =
                jdbc.sql(
                                TILES
                                        + "WHERE "
                                        + where.sql()
                                        + "\nORDER BY "
                                        + orderBy(query)
                                        + "\nLIMIT :limit OFFSET :offset")
                        .params(where.params())
                        .param("limit", ProductListQuery.PAGE_SIZE)
                        .param("offset", query.offset())
                        .query((rs, rowNum) -> toTile(rs, now))
                        .list();

        return PageResult.of(tiles, query.page(), ProductListQuery.PAGE_SIZE, total);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ProductFacetsView findFacets(
            ProductListQuery query, PriceBands priceBands, Instant now) {
        List<FacetCount> categories = query.category() == null ? categoryFacet(query, now) : null;
        return new ProductFacetsView(
                categories,
                enumFacet(query, now, Group.GENDER, "p.gender", Gender.values()),
                enumFacet(query, now, Group.SPORT, "p.sport", Sport.values()),
                colorFacet(query, now),
                sizeFacet(query, now),
                priceFacet(query, priceBands, now),
                onSaleFacet(query, now));
    }

    // ---------- Facets ----------

    private List<FacetCount> categoryFacet(ProductListQuery query, Instant now) {
        Where where = where(query, now, Group.CATEGORY);
        return jdbc.sql(
                        """
                        SELECT c.slug, c.name, count(*) AS n
                        FROM catalog.products p
                        JOIN catalog.categories c ON c.id = p.category_id
                        WHERE %s
                        GROUP BY c.id, c.slug, c.name, c.sort_order
                        ORDER BY c.sort_order, c.slug
                        """
                                .formatted(where.sql()))
                .params(where.params())
                .query((rs, rowNum) ->
                        new FacetCount(rs.getString("slug"), rs.getString("name"), rs.getLong("n")))
                .list();
    }

    /** Gender and sport: one column of the product; values in enum (display) order. */
    private <E extends Enum<E>> List<FacetCount> enumFacet(
            ProductListQuery query, Instant now, Group group, String column, E[] order) {
        Where where = where(query, now, group);
        Map<String, Long> counts =
                countsBy(
                        "SELECT %s AS value, count(*) AS n FROM catalog.products p WHERE %s GROUP BY %s"
                                .formatted(column, where.sql(), column),
                        where);
        return inOrder(counts, order);
    }

    /**
     * A product counts once per colour family it has. With a size chosen, the size must be in stock
     * in a colorway of the colour being counted, exactly like the filter "colour + size": the
     * product-level size clause is replaced by one tied to the grouped colorway.
     */
    private List<FacetCount> colorFacet(ProductListQuery query, Instant now) {
        Where where = where(query, now, Group.COLOR).without(Group.SIZE);
        String sizeInThisColorway =
                query.sizes().isEmpty()
                        ? ""
                        : "\n  AND EXISTS (SELECT 1 FROM catalog.variants vs WHERE vs.colorway_id"
                                + " = cw.id AND vs.available AND vs.size IN (:sizes))";
        Map<String, Long> counts =
                countsBy(
                        """
                        SELECT cw.color_family AS value, count(DISTINCT p.id) AS n
                        FROM catalog.products p
                        JOIN catalog.colorways cw ON cw.product_id = p.id
                        WHERE %s%s
                        GROUP BY cw.color_family
                        """
                                .formatted(where.sql(), sizeInThisColorway),
                        where);
        return inOrder(counts, ColorFamily.values());
    }

    /**
     * Only sizes in stock (FR-CAT-02), in size order. With a colour chosen, only sizes of colorways
     * of that colour count, like the size filter itself.
     */
    private List<FacetCount> sizeFacet(ProductListQuery query, Instant now) {
        Where where = where(query, now, Group.SIZE);
        String colorJoin =
                query.colors().isEmpty()
                        ? ""
                        : "JOIN catalog.colorways vc ON vc.id = v.colorway_id"
                                + " AND vc.color_family IN (:colors)\n";
        return jdbc.sql(
                        """
                        SELECT v.size, count(DISTINCT p.id) AS n
                        FROM catalog.products p
                        JOIN catalog.variants v ON v.product_id = p.id AND v.available
                        %s
                        WHERE %s
                        GROUP BY v.size
                        ORDER BY min(v.size_order), v.size
                        """
                                .formatted(colorJoin, where.sql()))
                .params(where.params())
                .params(colorParams(query))
                .query((rs, rowNum) -> new FacetCount(rs.getString("size"), null, rs.getLong("n")))
                .list();
    }

    /** One count per configured band, every band listed even at 0, in band order. */
    private List<FacetCount> priceFacet(ProductListQuery query, PriceBands bands, Instant now) {
        Where where = where(query, now, Group.PRICE);
        Map<String, Object> params = new HashMap<>(where.params());
        List<String> columns = new ArrayList<>();
        List<PriceBand> list = bands.bands();
        for (int i = 0; i < list.size(); i++) {
            columns.add(
                    "count(*) FILTER (WHERE %s) AS b%d"
                            .formatted(bandCondition(list.get(i), "facet" + i, params), i));
        }
        return jdbc.sql(
                        "SELECT %s FROM catalog.products p WHERE %s"
                                .formatted(String.join(", ", columns), where.sql()))
                .params(params)
                .query((rs, rowNum) -> {
                    List<FacetCount> counts = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        counts.add(
                                new FacetCount(list.get(i).toString(), null, rs.getLong("b" + i)));
                    }
                    return counts;
                })
                .single();
    }

    private long onSaleFacet(ProductListQuery query, Instant now) {
        Where where = where(query, now, Group.ON_SALE);
        return jdbc.sql(
                        "SELECT count(*) FROM catalog.products p WHERE "
                                + where.sql()
                                + " AND "
                                + CatalogSql.SALE_ACTIVE)
                .params(where.params())
                .query(Long.class)
                .single();
    }

    private Map<String, Long> countsBy(String sql, Where where) {
        return jdbc
                .sql(sql)
                .params(where.params())
                .query((rs, rowNum) -> Map.entry(rs.getString("value"), rs.getLong("n")))
                .list()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** Values with at least one product, in the enum's order. */
    private static <E extends Enum<E>> List<FacetCount> inOrder(
            Map<String, Long> counts, E[] order) {
        return Arrays.stream(order)
                .map(Enum::name)
                .filter(counts::containsKey)
                .map(name -> new FacetCount(name, null, counts.get(name)))
                .toList();
    }

    // ---------- WHERE from the query ----------

    /**
     * Products on sale (R-VIS) plus every chosen filter, except the group being counted. Values
     * always go in as :parameters, never into the SQL text.
     */
    private static Where where(ProductListQuery query, Instant now, Group except) {
        Where where = new Where();
        where.add(
                Group.FIXED,
                CatalogSql.ON_SALE,
                "now",
                OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
        if (query.category() != null) {
            where.add(
                    Group.FIXED,
                    "p.category_id = (SELECT id FROM catalog.categories WHERE slug = :category)",
                    "category",
                    query.category());
        }
        if (!query.categories().isEmpty()) {
            where.add(
                    Group.CATEGORY,
                    "p.category_id IN (SELECT id FROM catalog.categories WHERE slug IN"
                            + " (:categories))",
                    "categories",
                    query.categories());
        }
        if (!query.genders().isEmpty()) {
            where.add(Group.GENDER, "p.gender IN (:genders)", "genders", names(query.genders()));
        }
        if (!query.sports().isEmpty()) {
            where.add(Group.SPORT, "p.sport IN (:sports)", "sports", names(query.sports()));
        }
        boolean colorsApply = !query.colors().isEmpty() && except != Group.COLOR;
        if (colorsApply) {
            where.add(
                    Group.COLOR,
                    "EXISTS (SELECT 1 FROM catalog.colorways cw2 WHERE cw2.product_id = p.id"
                            + " AND cw2.color_family IN (:colors))",
                    "colors",
                    names(query.colors()));
        }
        if (!query.sizes().isEmpty()) {
            // With a colour chosen, the size must be in stock in a colorway of that colour.
            String colour =
                    colorsApply
                            ? " AND EXISTS (SELECT 1 FROM catalog.colorways vc2 WHERE vc2.id ="
                                    + " v3.colorway_id AND vc2.color_family IN (:colors))"
                            : "";
            where.add(
                    Group.SIZE,
                    "EXISTS (SELECT 1 FROM catalog.variants v3 WHERE v3.product_id = p.id"
                            + " AND v3.available AND v3.size IN (:sizes)"
                            + colour
                            + ")",
                    "sizes",
                    query.sizes());
        }
        if (!query.prices().isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            List<String> bands = new ArrayList<>();
            List<PriceBand> chosen = query.priceBands();
            for (int i = 0; i < chosen.size(); i++) {
                bands.add(bandCondition(chosen.get(i), "price" + i, params));
            }
            where.add(Group.PRICE, "(" + String.join(" OR ", bands) + ")", params);
        }
        if (query.onSaleOnly()) {
            where.add(Group.ON_SALE, CatalogSql.SALE_ACTIVE, Map.of());
        }
        return where.without(except);
    }

    /** min <= price < max on the effective price; the top band has no max. */
    private static String bandCondition(PriceBand band, String name, Map<String, Object> params) {
        params.put(name + "Min", band.min());
        String condition = CatalogSql.EFFECTIVE_PRICE + " >= :" + name + "Min";
        if (band.max() != null) {
            params.put(name + "Max", band.max());
            condition += " AND " + CatalogSql.EFFECTIVE_PRICE + " < :" + name + "Max";
        }
        return "(" + condition + ")";
    }

    private static Map<String, Object> colorParams(ProductListQuery query) {
        return query.colors().isEmpty() ? Map.of() : Map.of("colors", names(query.colors()));
    }

    private static List<String> names(List<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).toList();
    }

    // ---------- ORDER BY ----------

    /** Always ends with p.id, so equal keys never swap products between pages (FR-CAT-03). */
    private static String orderBy(ProductListQuery query) {
        return switch (query.sort()) {
            case FEATURED -> "p.featured_rank ASC NULLS LAST, p.launch_at DESC, p.id";
            case NEWEST -> "p.launch_at DESC, p.id";
            case PRICE_ASC -> CatalogSql.EFFECTIVE_PRICE + " ASC, p.id";
            case PRICE_DESC -> CatalogSql.EFFECTIVE_PRICE + " DESC, p.id";
        };
    }

    // ---------- One row to one tile ----------

    /**
     * Price and "Mới" come from the domain (ProductPricing, ProductPublication), not from the SQL
     * copy of the rules: SQL only filters and sorts by them, and the tests keep both in agreement.
     */
    private static ProductTileView toTile(ResultSet rs, Instant now) throws SQLException {
        Sale sale =
                rs.getObject("sale_price") == null
                        ? null
                        : new Sale(
                                rs.getLong("sale_price"),
                                instant(rs, "sale_starts_at"),
                                instant(rs, "sale_ends_at"));
        ProductImage image =
                rs.getString("image_key") == null
                        ? null
                        : new ProductImage(
                                rs.getString("image_key"),
                                rs.getInt("width"),
                                rs.getInt("height"),
                                rs.getString("alt"));
        var publication =
                new ProductPublication(
                        ProductStatus.valueOf(rs.getString("status")), instant(rs, "launch_at"));
        return new ProductTileView(
                rs.getString("slug"),
                rs.getString("name"),
                image,
                new ProductPricing(rs.getLong("list_price"), sale).priceAt(now),
                rs.getInt("color_count"),
                rs.getBoolean("in_stock"),
                publication.isNewAt(now));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    /**
     * WHERE clauses with their group and the values of their :parameters, kept together so a query
     * never gets one without the other.
     */
    private static final class Where {
        private final List<Map.Entry<Group, String>> clauses = new ArrayList<>();
        private final Map<String, Object> params = new HashMap<>();

        void add(Group group, String clause, String name, Object value) {
            add(group, clause, Map.of(name, value));
        }

        void add(Group group, String clause, Map<String, Object> values) {
            clauses.add(Map.entry(group, clause));
            params.putAll(values);
        }

        /** The same clauses without one group's; parameters are kept (unused ones are ignored). */
        Where without(Group group) {
            Where copy = new Where();
            clauses.stream().filter(clause -> clause.getKey() != group).forEach(copy.clauses::add);
            copy.params.putAll(params);
            return copy;
        }

        String sql() {
            return clauses.stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.joining("\n  AND "));
        }

        Map<String, Object> params() {
            return params;
        }
    }
}
