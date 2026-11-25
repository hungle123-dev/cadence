package com.cadence.catalog.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cadence.catalog.CatalogIT;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Colorway;
import com.cadence.catalog.domain.model.Fit;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Product;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.catalog.domain.model.Variant;
import com.cadence.catalog.domain.valueobject.ProductStatus;

class ProductPersistenceAdapterIntegrationTest extends CatalogIT {

    @Autowired private ProductPersistenceAdapter adapter;

    @Test
    void findBySlug_loadsTheWholeAggregateInDisplayOrder() {
        Product tempo = adapter.findBySlug("tempo-2").orElseThrow();

        assertThat(tempo.name()).isEqualTo("Cadence Tempo 2");
        assertThat(tempo.gender()).isEqualTo(Gender.MEN);
        assertThat(tempo.sport()).isEqualTo(Sport.RUNNING);
        assertThat(tempo.fit()).isEqualTo(Fit.TRUE_TO_SIZE);
        assertThat(tempo.featuredRank()).isEqualTo(1);
        assertThat(tempo.pricing().listPrice()).isEqualTo(3_290_000);
        assertThat(tempo.pricing().sale()).isNull();
        assertThat(tempo.publication().status()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(tempo.specs()).isNotEmpty();
        assertThat(tempo.colorways())
                .extracting(Colorway::colorFamily)
                .containsExactly(ColorFamily.GREEN, ColorFamily.BLACK, ColorFamily.GREY);

        Colorway first = tempo.colorways().getFirst();
        assertThat(first.images()).hasSize(3);
        assertThat(first.images().getFirst().imageKey())
                .isEqualTo("photo-1631542156377-473540702434");
        assertThat(first.variants())
                .extracting(Variant::size)
                .containsExactly("39", "40", "41", "42", "43", "44");
        assertThat(first.variants())
                .filteredOn(variant -> variant.size().equals("42"))
                .extracting(Variant::available)
                .containsExactly(false);
    }

    @Test
    void findBySlug_loadsHiddenProductsTooBecauseTheAggregateDecides() {
        assertThat(adapter.findBySlug("draft-runner"))
                .get()
                .extracting(product -> product.publication().status())
                .isEqualTo(ProductStatus.DRAFT);
        assertThat(adapter.findBySlug("khong-co")).isEmpty();
    }

    @Test
    void findBySlug_mapsATimeBoxedSale() {
        Product flash = adapter.findBySlug("flash-runner").orElseThrow();

        assertThat(flash.priceAt(NOW).amount()).isEqualTo(1_500_000);
        assertThat(flash.pricing().sale().endsAt()).isNotNull();
    }
}
