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
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.ProductTileSnapshotPage;
import com.cadence.catalog.application.port.driven.ProductTileReadPort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Sale;

/**
 * PLP tiles with native SQL (ADR-0011 allows it for complex reads). Filtering, sorting and paging happen in
 * PostgreSQL, so R-VIS, R-PRICE and R-STOCK are written here a second time and must match the domain:
 * ProductPublication.isVisibleAt (shared in CatalogSql), Sale.isActiveAt and "a variant without a stock flag is
 * sold out".
 */
public class ProductTileReadAdapter implements ProductTileReadPort {

    // ---------- (1) SQL copies of the domain rules ----------

    /** R-PRICE, same as Sale.isActiveAt: a sale runs in [starts, ends). */
    private static final String EFFECTIVE_PRICE = """
            (CASE WHEN c.sale_price IS NOT NULL AND c.sale_starts_at <= :now AND :now < c.sale_ends_at
                  THEN c.sale_price ELSE c.list_price END)""";

    /** R-STOCK: at least one size of the colorway has an in-stock flag. */
    private static final String ANY_SIZE_IN_STOCK = """
            EXISTS (SELECT 1 FROM catalog.variants v
                    JOIN catalog.variant_stock_flags f ON f.variant_id = v.id AND f.in_stock
                    WHERE v.colorway_id = c.id)""";

    /** AC 2.1: the chosen sizes must be in stock, not merely exist. */
    private static final String CHOSEN_SIZE_IN_STOCK = """
            EXISTS (SELECT 1 FROM catalog.variants v
                    JOIN catalog.variant_stock_flags f ON f.variant_id = v.id AND f.in_stock
                    WHERE v.colorway_id = c.id AND v.size IN (:sizes))""";

    private static final String FROM = """
            FROM catalog.colorways c
            JOIN catalog.products p ON p.id = c.product_id
            """;

    /** One row per colorway; the LATERAL join picks its first image (null when it has none). */
    private static final String SELECT_TILES = """
            SELECT p.slug AS product_slug, c.slug AS colorway_slug, p.name AS product_name, c.name AS colorway_name,
                   c.list_price, c.sale_price, c.sale_starts_at, c.sale_ends_at,
                   (SELECT count(*) FROM catalog.colorways c2 WHERE c2.product_id = p.id) AS color_count,
                   %s AS in_stock,
                   i.storage_key, i.alt, i.width, i.height
            %s
            LEFT JOIN LATERAL (
                SELECT pi.storage_key, pi.alt, pi.width, pi.height
                FROM catalog.product_images pi
                WHERE pi.colorway_id = c.id
                ORDER BY pi.position, pi.id
                LIMIT 1
            ) i ON true
            """.formatted(ANY_SIZE_IN_STOCK, FROM);

    /** AC 2.12: sizes in size_order, not alphabetical; only sizes someone can still buy (A11). */
    private static final String SIZES_IN_STOCK = """
            SELECT v.size
            FROM catalog.variants v
            JOIN catalog.variant_stock_flags f ON f.variant_id = v.id AND f.in_stock
            JOIN catalog.colorways c ON c.id = v.colorway_id
            JOIN catalog.products p ON p.id = c.product_id
            WHERE %s
            GROUP BY v.size
            ORDER BY min(v.size_order), v.size
            """;

    /** One row, even for an empty category: aggregates over no rows give NULL (A13). */
    private static final String OTHER_OPTIONS = """
            SELECT array_agg(DISTINCT c.color_family)        AS colors,
                   bool_or(p.gender IN ('MEN', 'UNISEX'))    AS for_men,
                   bool_or(p.gender IN ('WOMEN', 'UNISEX'))  AS for_women,
                   min(%1$s) AS min_price,
                   max(%1$s) AS max_price
            %2$s
            WHERE %3$s
            """;

    private final JdbcClient jdbc;

    public ProductTileReadAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- (3) two queries, one snapshot ----------

    /** Count, then page; REPEATABLE READ makes both read the same snapshot, so the total matches the page. */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ProductTileSnapshotPage findTiles(ProductTileQuery query, Set<UUID> categoryIds, Instant now) {
        Where where = where(query, categoryIds, now);

        long totalItems = jdbc.sql("SELECT count(*) " + FROM + "WHERE " + where.sql())
                .params(where.params())
                .query(Long.class)
                .single();

        List<ProductTileSnapshotPage.Tile> tiles = jdbc.sql(SELECT_TILES
                        + "WHERE " + where.sql()
                        + "\nORDER BY " + orderBy(query)
                        + "\nLIMIT :limit OFFSET :offset")
                .params(where.params())
                .param("limit", ProductTileQuery.PAGE_SIZE)
                .param("offset", query.offset())
                .query((rs, rowNum) -> toTile(rs))
                .list();

        return new ProductTileSnapshotPage(tiles, totalItems);
    }

    /** Filter values of the categories (A1): not narrowed by the current filters, so the panel never jumps. */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ProductTilePageView.FilterOptions findFilterOptions(Set<UUID> categoryIds, Instant now) {
        Where where = visibleIn(categoryIds, now);

        List<String> sizes = jdbc.sql(SIZES_IN_STOCK.formatted(where.sql()))
                .params(where.params())
                .query(String.class)
                .list();

        return jdbc.sql(OTHER_OPTIONS.formatted(EFFECTIVE_PRICE, FROM, where.sql()))
                .params(where.params())
                .query((rs, rowNum) -> toFilterOptions(rs, sizes))
                .single();
    }

    // ---------- (2) WHERE from the filters that are set ----------

    /** Visible products (R-VIS) in the given categories: what both the tiles and the filter options start from. */
    private static Where visibleIn(Set<UUID> categoryIds, Instant now) {
        Where where = new Where();
        where.add(CatalogSql.VISIBLE, "now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
        if (!categoryIds.isEmpty()) {
            where.add("p.category_id IN (:categoryIds)", "categoryIds", List.copyOf(categoryIds));
        }
        return where;
    }

    /** visibleIn plus the filters that are set; values always go in as :parameters, never into the string. */
    private static Where where(ProductTileQuery query, Set<UUID> categoryIds, Instant now) {
        Where where = visibleIn(categoryIds, now);
        if (!query.colors().isEmpty()) {
            where.add("c.color_family IN (:colors)", "colors", query.colors().stream().map(Enum::name).toList());
        }
        if (query.gender() != null) {
            where.add("p.gender IN (:gender, 'UNISEX')", "gender", query.gender().name()); // AC 2.5
        }
        if (query.minPrice() != null) {
            where.add(EFFECTIVE_PRICE + " >= :minPrice", "minPrice", query.minPrice());
        }
        if (query.maxPrice() != null) {
            where.add(EFFECTIVE_PRICE + " <= :maxPrice", "maxPrice", query.maxPrice());
        }
        if (!query.sizes().isEmpty()) {
            where.add(CHOSEN_SIZE_IN_STOCK, "sizes", query.sizes());
        }
        return where;
    }

    /** Always ends with unique columns, so equal prices or dates never swap tiles between pages (A3, AC 1.8). */
    private static String orderBy(ProductTileQuery query) {
        return switch (query.sort()) {
            case NEWEST -> "p.publish_at DESC, p.id, c.position, c.id";
            case PRICE_ASC -> EFFECTIVE_PRICE + " ASC, c.id";
            case PRICE_DESC -> EFFECTIVE_PRICE + " DESC, c.id";
        };
    }

    // ---------- (4) one row -> one Tile ----------

    private static ProductTileSnapshotPage.Tile toTile(ResultSet rs) throws SQLException {
        Sale sale = rs.getObject("sale_price") == null ? null
                : new Sale(rs.getLong("sale_price"), instant(rs, "sale_starts_at"), instant(rs, "sale_ends_at"));
        ProductSnapshot.Image image = rs.getString("storage_key") == null ? null
                : new ProductSnapshot.Image(rs.getString("storage_key"), rs.getString("alt"),
                        rs.getInt("width"), rs.getInt("height"));
        return new ProductTileSnapshotPage.Tile(
                rs.getString("product_slug"),
                rs.getString("colorway_slug"),
                rs.getString("product_name"),
                rs.getString("colorway_name"),
                image,
                new ColorwayPricing(rs.getLong("list_price"), sale),
                rs.getInt("color_count"),
                rs.getBoolean("in_stock"));
    }

    private static ProductTilePageView.FilterOptions toFilterOptions(ResultSet rs, List<String> sizes)
            throws SQLException {
        java.sql.Array colorArray = rs.getArray("colors"); // NULL when the category is empty
        List<ColorFamily> colors = colorArray == null ? List.of()
                : Arrays.stream((String[]) colorArray.getArray())
                        .map(ColorFamily::valueOf)
                        .sorted() // enum order: BLACK, WHITE, GREY…
                        .toList();

        List<Gender> genders = new ArrayList<>(); // only filterable values (A12)
        if (rs.getBoolean("for_men")) {
            genders.add(Gender.MEN);
        }
        if (rs.getBoolean("for_women")) {
            genders.add(Gender.WOMEN);
        }

        // getLong reads NULL as 0, which is exactly the empty-category range (A13)
        var priceRange = new ProductTilePageView.PriceRange(rs.getLong("min_price"), rs.getLong("max_price"));
        return new ProductTilePageView.FilterOptions(sizes, colors, List.copyOf(genders), priceRange);
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class).toInstant();
    }

    /** WHERE clauses and the values for their :parameters, kept together so a query never gets one without the other. */
    private static final class Where {
        private final List<String> clauses = new ArrayList<>();
        private final Map<String, Object> params = new HashMap<>();

        void add(String clause, String name, Object value) {
            clauses.add(clause);
            params.put(name, value);
        }

        String sql() {
            return String.join("\n  AND ", clauses);
        }

        Map<String, Object> params() {
            return params;
        }
    }
}
