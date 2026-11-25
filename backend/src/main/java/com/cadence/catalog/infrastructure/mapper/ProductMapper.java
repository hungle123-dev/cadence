package com.cadence.catalog.infrastructure.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cadence.catalog.domain.model.Product;
import com.cadence.catalog.domain.model.Spec;
import com.cadence.catalog.domain.valueobject.ProductPricing;
import com.cadence.catalog.domain.valueobject.Sale;
import com.cadence.catalog.infrastructure.persistence.entity.ProductJpaEntity;

/**
 * JPA rows to the Product aggregate. Fields with the same name map themselves (colorways, images,
 * variants), text columns become enums; only what has no twin is written here.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /** status + launchAt of the row map by name onto ProductPublication (R-VIS). */
    @Mapping(target = "publication", source = ".")
    @Mapping(target = "pricing", source = ".")
    Product toDomain(ProductJpaEntity product);

    /** Four price columns become one pricing; the sale is all set or absent (DB check). */
    default ProductPricing toPricing(ProductJpaEntity product) {
        Sale sale =
                product.getSalePrice() == null
                        ? null
                        : new Sale(
                                product.getSalePrice(),
                                product.getSaleStartsAt(),
                                product.getSaleEndsAt());
        return new ProductPricing(product.getListPrice(), sale);
    }

    default List<Spec> toSpecs(List<Map<String, String>> specs) {
        return specs.stream().map(spec -> new Spec(spec.get("name"), spec.get("value"))).toList();
    }
}
