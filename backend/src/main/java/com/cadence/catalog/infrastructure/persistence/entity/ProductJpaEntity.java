package com.cadence.catalog.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.cadence.common.infrastructure.persistence.AuditEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Row of catalog.products. Read-only until admin editing exists (FR-ADM-PRD): no setters. */
@Entity
@Table(schema = "catalog", name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductJpaEntity extends AuditEntity {

    @Id private UUID id;

    private String slug;

    private String name;

    @Column(name = "category_id")
    private UUID categoryId;

    private String gender;

    private String sport;

    private String description;

    /** [{"name": ..., "value": ...}] in display order (a JSON object would lose the order). */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> specs;

    private String status;

    @Column(name = "launch_at")
    private Instant launchAt;

    @Column(name = "list_price")
    private long listPrice;

    @Column(name = "sale_price")
    private Long salePrice;

    @Column(name = "sale_starts_at")
    private Instant saleStartsAt;

    @Column(name = "sale_ends_at")
    private Instant saleEndsAt;

    @Column(name = "featured_rank")
    private Integer featuredRank;

    private String fit;

    private BigDecimal rating;

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_description")
    private String seoDescription;

    @OneToMany(mappedBy = "product")
    @OrderBy("sortOrder")
    private List<ColorwayJpaEntity> colorways;

    // Written by hand so callers get a read-only view; Lombok skips a getter that exists.
    public List<Map<String, String>> getSpecs() {
        return Collections.unmodifiableList(specs);
    }

    public List<ColorwayJpaEntity> getColorways() {
        return Collections.unmodifiableList(colorways);
    }
}
