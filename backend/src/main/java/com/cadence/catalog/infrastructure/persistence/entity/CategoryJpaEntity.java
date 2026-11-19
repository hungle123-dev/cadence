package com.cadence.catalog.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "catalog", name = "categories") 
public class CategoryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    private String slug;
    private String name;
    private short depth;   
    private int position;

    protected CategoryJpaEntity() {
        // JPA cần constructor rỗng
    }

    public UUID getId() { return id; }
    public UUID getParentId() { return parentId; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public short getDepth() { return depth; }
    public int getPosition() { return position; }
}