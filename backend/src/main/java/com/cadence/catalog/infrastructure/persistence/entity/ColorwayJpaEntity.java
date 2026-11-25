package com.cadence.catalog.infrastructure.persistence.entity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Row of catalog.colorways. Its version and timestamps live on the product (one aggregate). */
@Entity
@Table(schema = "catalog", name = "colorways")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColorwayJpaEntity {

    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @Getter(AccessLevel.NONE)
    private ProductJpaEntity product;

    private String name;

    @Column(name = "color_family")
    private String colorFamily;

    @Column(name = "sort_order")
    private int sortOrder;

    @OneToMany(mappedBy = "colorway")
    @OrderBy("sortOrder")
    private List<ProductImageJpaEntity> images;

    @OneToMany(mappedBy = "colorway")
    @OrderBy("sizeOrder")
    private List<VariantJpaEntity> variants;

    // Written by hand so callers get a read-only view; Lombok skips a getter that exists.
    public List<ProductImageJpaEntity> getImages() {
        return Collections.unmodifiableList(images);
    }

    public List<VariantJpaEntity> getVariants() {
        return Collections.unmodifiableList(variants);
    }
}
