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
@Table(schema = "catalog", name = "product_images")
public class ProductImageJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colorway_id")
    private ColorwayJpaEntity colorway;

    @Column(name = "storage_key")
    private String storageKey;

    private String alt;
    private int width;
    private int height;
    private int position;

    protected ProductImageJpaEntity() {
        // JPA cần constructor rỗng
    }

    public String getStorageKey() { return storageKey; }
    public String getAlt() { return alt; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getPosition() { return position; }
}
