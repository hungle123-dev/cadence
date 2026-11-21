package com.cadence.catalog.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(schema = "catalog", name = "products")
public class ProductJpaEntity {

    @Id
    private UUID id;

    private String slug;
    private String name;
    private String description;
    private String type;
    private String gender;
    private String sport;

    @Column(name = "category_id")
    private UUID categoryId;

    private String status;

    @Column(name = "publish_at")
    private Instant publishAt;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> specs;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "product")
    @OrderBy("position")
    private List<ColorwayJpaEntity> colorways;

    protected ProductJpaEntity() {
        // JPA cần constructor rỗng
    }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getGender() { return gender; }
    public String getSport() { return sport; }
    public UUID getCategoryId() { return categoryId; }
    public String getStatus() { return status; }
    public Instant getPublishAt() { return publishAt; }
    public Map<String, String> getSpecs() { return specs; }
    public List<ColorwayJpaEntity> getColorways() { return colorways; }
}
