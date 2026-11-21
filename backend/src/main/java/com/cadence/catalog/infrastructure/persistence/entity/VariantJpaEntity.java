package com.cadence.catalog.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "catalog", name = "variants")
public class VariantJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colorway_id")
    private ColorwayJpaEntity colorway;

    private String sku;
    private String size;

    @Column(name = "size_order")
    private int sizeOrder;

    protected VariantJpaEntity() {
        // JPA cần constructor rỗng
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getSize() { return size; }
    public int getSizeOrder() { return sizeOrder; }
}
