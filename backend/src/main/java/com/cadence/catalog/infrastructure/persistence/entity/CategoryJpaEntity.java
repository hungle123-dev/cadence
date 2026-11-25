package com.cadence.catalog.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Row of catalog.categories. Read-only: categories come from the sample data (SRS 3.1). */
@Entity
@Table(schema = "catalog", name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryJpaEntity {

    @Id private UUID id;

    private String slug;

    private String name;

    @Column(name = "sort_order")
    private int sortOrder;
}
