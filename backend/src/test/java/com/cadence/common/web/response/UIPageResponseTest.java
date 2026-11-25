package com.cadence.common.web.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.cadence.common.application.PageResult;

class UIPageResponseTest {

    @Test
    void from_copiesThePageAndConvertsItems() {
        var page = PageResult.of(List.of("tempo", "pace"), 2, 12, 14);

        UIPageResponse<Integer> response = UIPageResponse.from(page, String::length);

        assertThat(response.number()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(12);
        assertThat(response.totalElements()).isEqualTo(14);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.numberOfElements()).isEqualTo(2);
        assertThat(response.content()).containsExactly(5, 4);
    }

    @Test
    void from_withoutMapperKeepsTheItems() {
        var page = PageResult.of(List.of("tempo"), 1, 12, 1);

        assertThat(UIPageResponse.from(page).content()).containsExactly("tempo");
    }

    @Test
    void of_wrapsOneObjectUnderData() {
        assertThat(UIApiResponse.of("tempo").data()).isEqualTo("tempo");
    }
}
