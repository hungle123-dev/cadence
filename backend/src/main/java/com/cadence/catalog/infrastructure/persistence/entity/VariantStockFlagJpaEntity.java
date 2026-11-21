package com.cadence.catalog.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "catalog", name = "variant_stock_flags")
public class VariantStockFlagJpaEntity {

    @Id
    @Column(name = "variant_id")
    private UUID variantId;

    private String sku;

    @Column(name = "in_stock")
    private boolean inStock;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected VariantStockFlagJpaEntity() {
        // JPA cần constructor rỗng
    }

    public UUID getVariantId() { return variantId; }
    public boolean isInStock() { return inStock; }
}
