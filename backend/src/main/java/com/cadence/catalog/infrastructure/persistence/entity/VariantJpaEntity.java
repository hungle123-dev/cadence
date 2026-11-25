package com.cadence.catalog.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.cadence.common.infrastructure.persistence.AuditEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Row of catalog.variants. Has its own version and timestamps: {@code available} is updated from
 * inventory events (architecture 7.8) without touching the product row.
 */
@Entity
@Table(schema = "catalog", name = "variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VariantJpaEntity extends AuditEntity {

    @Id private UUID id;

    @Column(name = "product_id")
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colorway_id")
    @Getter(AccessLevel.NONE)
    private ColorwayJpaEntity colorway;

    private String size;

    @Column(name = "size_order")
    private int sizeOrder;

    private String sku;

    private String gtin;

    private boolean available;
}
