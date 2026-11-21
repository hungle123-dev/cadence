package com.cadence.catalog.infrastructure.adapter;

/**
 * SQL copies of catalog rules shared by the SQL adapters, so each rule has one Java version (domain) and one SQL
 * version (here). Every query using these joins catalog.products as p.
 */
final class CatalogSql {

    /** R-VIS, same as ProductPublication.isVisibleAt. Needs the :now parameter. */
    static final String VISIBLE =
            "(p.status = 'PUBLISHED' OR (p.status = 'SCHEDULED' AND p.publish_at <= :now))";

    private CatalogSql() {
    }
}
