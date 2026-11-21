package com.cadence.catalog.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.service.CategoryQueryService;

/**
 * Use case test: the read port is replaced by a fake, so no database and no Spring are needed.
 */
class CategoryQueryServiceTest {

    @Test
    void returns_categories_in_the_order_given_by_the_read_port() {
        var shoes = new CategoryView(UUID.randomUUID(), null, "giay", "Giày", 1, 1);
        var runningShoes = new CategoryView(UUID.randomUUID(), shoes.id(), "giay-chay-bo", "Giày chạy bộ", 2, 1);
        CategoryReadPort fakePort = () -> List.of(shoes, runningShoes);

        var service = new CategoryQueryService(fakePort);

        assertEquals(List.of(shoes, runningShoes), service.listCategories());
    }

    @Test
    void returns_an_empty_list_when_there_are_no_categories() {
        CategoryReadPort fakePort = List::of;

        var service = new CategoryQueryService(fakePort);

        assertTrue(service.listCategories().isEmpty());
    }
}
