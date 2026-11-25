package com.cadence.catalog.infrastructure.restapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.cadence.catalog.CatalogIT;

class CatalogControllerIntegrationTest extends CatalogIT {

    @Autowired private MockMvcTester mvc;

    @Test
    void categories_listsEveryCategoryInDisplayOrder() {
        var body = assertThat(mvc.get().uri("/v1/catalog/categories")).hasStatusOk().bodyJson();

        body.extractingPath("$.data.length()").isEqualTo(18);
        body.extractingPath("$.data[0].slug").isEqualTo("giay-chay-bo");
    }

    @Test
    @DisplayName("TC-CAT-01, TC-CAT-15 Trang danh mục: 12 sản phẩm, URL ảnh ghép từ khoá ảnh")
    void list_answersAPageOfTilesWithImageUrls() {
        var body =
                assertThat(mvc.get().uri("/v1/catalog/products").param("category", "giay-chay-bo"))
                        .hasStatusOk()
                        .bodyJson();

        body.extractingPath("$.number").isEqualTo(1);
        body.extractingPath("$.size").isEqualTo(12);
        body.extractingPath("$.content[0].slug").isEqualTo("tempo-2");
        body.extractingPath("$.content[0].image.url")
                .isEqualTo(
                        "https://images.unsplash.com/photo-1631542156377-473540702434"
                                + "?auto=format&fit=crop&q=80&w=1600&h=1600");
        body.extractingPath("$.content[0].price.amount").isEqualTo(3_290_000);
        body.extractingPath("$.content[0].isNew").isEqualTo(true);
    }

    @Test
    @DisplayName("Sai kiểu dữ liệu: VALIDATION_ERROR, báo mọi tham số sai cùng lúc")
    void list_reportsEveryWrongTypeAtOnce() {
        var body =
                assertThat(
                                mvc.get()
                                        .uri("/v1/catalog/products")
                                        .param("page", "abc")
                                        .param("color", "RED")
                                        .param("sort", "CHEAPEST"))
                        .hasStatus(400)
                        .bodyJson();

        body.extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        body.extractingPath("$.invalidParams.length()").isEqualTo(3);
    }

    @Test
    @DisplayName("Sai giá trị: CATALOG_INVALID_FILTER")
    void list_rejectsWrongValuesWithTheCatalogCode() {
        assertThat(mvc.get().uri("/v1/catalog/products").param("page", "0"))
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("CATALOG_INVALID_FILTER");
        assertThat(
                        mvc.get()
                                .uri("/v1/catalog/products")
                                .param("category", "giay-chay-bo")
                                .param("categories", "giay-trail"))
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.invalidParams[0].name")
                .isEqualTo("categories");
        assertThat(mvc.get().uri("/v1/catalog/products").param("price", "0-500000"))
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.invalidParams[0].name")
                .isEqualTo("price");
    }

    @Test
    void list_answers404ForAnUnknownCategoryPage() {
        assertThat(mvc.get().uri("/v1/catalog/products").param("category", "khong-co"))
                .hasStatus(404)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("CATALOG_CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("TC-CAT-18 Trang danh mục không có facet danh mục; trang tất cả sản phẩm có")
    void facets_leaveOutCategoriesOnACategoryPage() {
        var onCategoryPage =
                assertThat(
                                mvc.get()
                                        .uri("/v1/catalog/products/facets")
                                        .param("category", "giay-chay-bo"))
                        .hasStatusOk()
                        .bodyJson();
        onCategoryPage.extractingPath("$.data.categories").isNull();
        onCategoryPage.extractingPath("$.data.prices.length()").isEqualTo(4);

        assertThat(mvc.get().uri("/v1/catalog/products/facets"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.data.categories[0].label")
                .isEqualTo("Giày chạy bộ");
    }

    @Test
    @DisplayName("TC-CAT-11, TC-SEO-06 PDP: phối màu, size, giá, SEO mặc định")
    void detail_answersTheProductPage() {
        var body =
                assertThat(mvc.get().uri("/v1/catalog/products/tempo-2")).hasStatusOk().bodyJson();

        body.extractingPath("$.data.visibility").isEqualTo("ON_SALE");
        body.extractingPath("$.data.category.slug").isEqualTo("giay-chay-bo");
        body.extractingPath("$.data.seo.title")
                .isEqualTo("Cadence Tempo 2 – Giày chạy bộ | Cadence");
        body.extractingPath("$.data.colorways.length()").isEqualTo(3);
        body.extractingPath("$.data.colorways[0].sizes[3].size").isEqualTo("42");
        body.extractingPath("$.data.colorways[0].sizes[3].available").isEqualTo(false);
        body.extractingPath("$.data.colorways[0].images[0].width").isEqualTo(1600);
    }

    @Test
    @DisplayName("TC-SEO-05 Sắp mở bán: 200 kèm giờ mở bán; nháp, ngừng bán, không tồn tại: 404")
    void detail_followsTheVisibilityRules() {
        var comingSoon =
                assertThat(mvc.get().uri("/v1/catalog/products/future-runner"))
                        .hasStatusOk()
                        .bodyJson();
        comingSoon.extractingPath("$.data.visibility").isEqualTo("COMING_SOON");
        comingSoon.extractingPath("$.data.launchAt").isEqualTo("2026-11-25T13:00:00Z");

        for (String slug : new String[] {"draft-runner", "archived-runner", "khong-co"}) {
            assertThat(mvc.get().uri("/v1/catalog/products/" + slug))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.code")
                    .isEqualTo("CATALOG_PRODUCT_NOT_FOUND");
        }
    }

    @Test
    @DisplayName("TC-SEO-03 Sitemap chỉ có sản phẩm đang bán")
    void sitemap_listsOnlyProductsOnSale() {
        var body = assertThat(mvc.get().uri("/v1/catalog/sitemap")).hasStatusOk().bodyJson();

        body.extractingPath("$.data.products[*].slug")
                .asArray()
                .contains("tempo-2", "flash-runner")
                .doesNotContain("future-runner", "draft-runner", "archived-runner");
        body.extractingPath("$.data.categories[0].slug").isEqualTo("giay-chay-bo");
    }
}
