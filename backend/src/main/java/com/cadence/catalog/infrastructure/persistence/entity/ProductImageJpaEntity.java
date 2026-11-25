package com.cadence.catalog.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Row of catalog.product_images: an image key, never a URL (BR-CAT-01). */
@Entity
@Table(schema = "catalog", name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImageJpaEntity {

    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colorway_id")
    @Getter(AccessLevel.NONE)
    private ColorwayJpaEntity colorway;

    @Column(name = "image_key")
    private String imageKey;

    private int width;

    private int height;

    private String alt;

    @Column(name = "sort_order")
    private int sortOrder;
}
