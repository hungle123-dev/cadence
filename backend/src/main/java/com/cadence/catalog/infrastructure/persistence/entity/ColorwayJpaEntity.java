package com.cadence.catalog.infrastructure.persistence.entity;

import java.time.Instant;
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

@Entity
@Table(schema = "catalog", name = "colorways")
public class ColorwayJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductJpaEntity product;

    private String slug;
    private String name;

    @Column(name = "color_family")
    private String colorFamily;

    @Column(name = "list_price")
    private long listPrice;

    @Column(name = "sale_price")
    private Long salePrice;

    @Column(name = "sale_starts_at")
    private Instant saleStartsAt;

    @Column(name = "sale_ends_at")
    private Instant saleEndsAt;

    private int position;

    @OneToMany(mappedBy = "colorway")
    @OrderBy("sizeOrder")
    private List<VariantJpaEntity> variants;

    @OneToMany(mappedBy = "colorway")
    @OrderBy("position")
    private List<ProductImageJpaEntity> images;

    protected ColorwayJpaEntity() {
        // JPA cần constructor rỗng
    }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getColorFamily() { return colorFamily; }
    public long getListPrice() { return listPrice; }
    public Long getSalePrice() { return salePrice; }
    public Instant getSaleStartsAt() { return saleStartsAt; }
    public Instant getSaleEndsAt() { return saleEndsAt; }
    public int getPosition() { return position; }
    public List<VariantJpaEntity> getVariants() { return variants; }
    public List<ProductImageJpaEntity> getImages() { return images; }
}
