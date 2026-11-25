package com.cadence.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cadence.catalog.domain.valueobject.ProductPricing;
import com.cadence.catalog.domain.valueobject.ProductPublication;
import com.cadence.catalog.domain.valueobject.ProductStatus;

class ProductTest {

    private static final Instant LAUNCH = Instant.parse("2026-10-01T03:00:00Z");

    @Test
    @DisplayName(
            "TC-SEO-06 Tiêu đề mặc định là \"{tên} – {danh mục} | Cadence\"; ghi đè thì dùng ghi"
                    + " đè")
    void seoTitle_defaultsToNameAndCategory() {
        assertThat(product("Mô tả", null, null).seoTitle("Giày chạy bộ"))
                .isEqualTo("Cadence Tempo 2 – Giày chạy bộ | Cadence");
        assertThat(product("Mô tả", "Tempo 2 chính hãng", null).seoTitle("Giày chạy bộ"))
                .isEqualTo("Tempo 2 chính hãng");
    }

    @Test
    @DisplayName("TC-SEO-06 Mô tả mặc định tối đa 155 ký tự, cắt ở ranh giới từ")
    void seoDescription_cutsTheDescriptionAtAWordBoundary() {
        String longText = "Giày chạy hằng ngày ".repeat(20);

        String description = product(longText, null, null).seoDescription();

        assertThat(description).hasSizeLessThanOrEqualTo(Product.SEO_DESCRIPTION_LENGTH);
        assertThat(longText).startsWith(description);
        assertThat(longText.charAt(description.length())).isEqualTo(' '); // a whole word
        assertThat(product("  Ngắn   gọn  ", null, null).seoDescription()).isEqualTo("Ngắn gọn");
        assertThat(product("", null, null).seoDescription()).isEqualTo("Cadence Tempo 2");
        assertThat(product("Mô tả", null, "Ghi đè").seoDescription()).isEqualTo("Ghi đè");
    }

    @Test
    @DisplayName("TC-CAT-16 Hết hàng khi mọi size của mọi phối màu có ATS = 0")
    void isInStock_needsOneAvailableSizeInAnyColorway() {
        assertThat(withSizes(false, false).isInStock()).isFalse();
        assertThat(withSizes(false, true).isInStock()).isTrue();
    }

    @Test
    void productImage_rejectsAFullUrlBecauseOnlyKeysAreStored() {
        assertThatThrownBy(() -> new ProductImage("https://cdn.example/a.jpg", 10, 10, "Ảnh"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProductImage("a.jpg", 0, 10, "Ảnh"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Product product(String description, String seoTitle, String seoDescription) {
        return new Product(
                UUID.randomUUID(),
                "tempo-2",
                "Cadence Tempo 2",
                UUID.randomUUID(),
                Gender.MEN,
                Sport.RUNNING,
                description,
                List.of(new Spec("Trọng lượng", "228 g")),
                Fit.TRUE_TO_SIZE,
                1,
                null,
                0,
                seoTitle,
                seoDescription,
                new ProductPricing(3_290_000, null),
                new ProductPublication(ProductStatus.PUBLISHED, LAUNCH),
                List.of());
    }

    private static Product withSizes(boolean first, boolean second) {
        Colorway black = colorway(first);
        Colorway white = colorway(second);
        Product base = product("Mô tả", null, null);
        return new Product(
                base.id(),
                base.slug(),
                base.name(),
                base.categoryId(),
                base.gender(),
                base.sport(),
                base.description(),
                base.specs(),
                base.fit(),
                base.featuredRank(),
                base.rating(),
                base.reviewCount(),
                null,
                null,
                base.pricing(),
                base.publication(),
                List.of(black, white));
    }

    private static Colorway colorway(boolean available) {
        return new Colorway(
                UUID.randomUUID(),
                "Màu " + UUID.randomUUID(),
                ColorFamily.BLACK,
                List.of(new ProductImage("photo-1", 1600, 1600, "Ảnh")),
                List.of(
                        new Variant(
                                UUID.randomUUID(), "SKU-" + UUID.randomUUID(), "42", available)));
    }
}
