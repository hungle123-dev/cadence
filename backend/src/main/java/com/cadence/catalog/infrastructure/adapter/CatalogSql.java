package com.cadence.catalog.infrastructure.adapter;

/**
 * SQL copies of catalog rules shared by the JDBC adapters, so each rule has one Java version
 * (domain) and one SQL version (here). Every query using these names catalog.products as p and
 * binds :now. CatalogSqlRulesIntegrationTest checks that both versions agree.
 */
final class CatalogSql {

    /** R-VIS on sale, same as ProductPublication.visibilityAt == ON_SALE. */
    static final String ON_SALE = "(p.status = 'PUBLISHED' AND p.launch_at <= :now)";

    /** BR-PRC-09, same as Sale.isActiveAt: active in [starts, ends). */
    static final String SALE_ACTIVE =
            "(p.sale_price IS NOT NULL AND p.sale_starts_at <= :now"
                    + " AND (p.sale_ends_at IS NULL OR :now < p.sale_ends_at))";

    /** R-PRICE, same as ProductPricing.priceAt. */
    static final String EFFECTIVE_PRICE =
            "(CASE WHEN " + SALE_ACTIVE + " THEN p.sale_price ELSE p.list_price END)";

    private CatalogSql() {}
}
