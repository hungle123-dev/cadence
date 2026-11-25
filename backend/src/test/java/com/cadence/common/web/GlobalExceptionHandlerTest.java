package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.common.domain.error.CommonErrorCode;
import com.cadence.common.domain.error.DomainException;
import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import com.cadence.common.domain.error.InvalidParam;
import com.cadence.common.domain.error.ResourceNotFoundException;
import com.cadence.common.domain.error.ValidationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc =
                MockMvcBuilders.standaloneSetup(new ThrowingController())
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .addFilter(new RequestIdFilter())
                        .build();
    }

    @Test
    void notFoundDomainError_becomes404WithItsCodeAndRequestId() throws Exception {
        mvc.perform(get("/not-found").header(RequestIdFilter.HEADER, "req-12345678"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEST_THING_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Không tìm thấy thứ cần tìm"))
                .andExpect(jsonPath("$.requestId").value("req-12345678"));
    }

    @Test
    void businessRuleError_becomes422() throws Exception {
        mvc.perform(get("/business-rule"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("TEST_RULE_BROKEN"));
    }

    @Test
    void moduleValidationError_listsEveryInvalidParam() throws Exception {
        mvc.perform(get("/invalid-filter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEST_INVALID_FILTER"))
                .andExpect(jsonPath("$.invalidParams.length()").value(2))
                .andExpect(jsonPath("$.invalidParams[0].name").value("minPrice"))
                .andExpect(jsonPath("$.invalidParams[1].name").value("page"));
    }

    @Test
    void optimisticLockClash_becomes409() throws Exception {
        mvc.perform(get("/concurrent"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONCURRENT_UPDATE"));
    }

    @Test
    void unexpectedError_becomes500WithoutLeakingDetails() throws Exception {
        mvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(
                        jsonPath("$.detail")
                                .value(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage()));
    }

    @Test
    void invalidBody_becomes400WithTheInvalidFields() throws Exception {
        mvc.perform(
                        post("/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.invalidParams[0].name").value("name"));
    }

    @Test
    void unconvertibleParameter_becomes400NamingTheParameter() throws Exception {
        mvc.perform(get("/paged").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.invalidParams[0].name").value("page"))
                .andExpect(
                        jsonPath("$.invalidParams[0].reason")
                                .value(GlobalExceptionHandler.UNREADABLE_VALUE));
    }

    @Test
    void constraintOnAParameter_becomes400NamingTheParameter() throws Exception {
        mvc.perform(get("/sized").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.invalidParams[0].name").value("size"));
    }

    @Test
    void codeFor_derivesTheCodeFromTheStatus() {
        assertThat(GlobalExceptionHandler.codeFor(HttpStatusCode.valueOf(404)))
                .isEqualTo("NOT_FOUND");
        assertThat(GlobalExceptionHandler.codeFor(HttpStatusCode.valueOf(503)))
                .isEqualTo("INTERNAL_ERROR");
        assertThat(GlobalExceptionHandler.codeFor(HttpStatusCode.valueOf(499)))
                .isEqualTo("HTTP_499");
    }

    @Test
    void apiProblem_keepsAnImmutableCopyOfTheInvalidParams() {
        List<ApiProblem.Param> params = new ArrayList<>(List.of(new ApiProblem.Param("a", "b")));
        ApiProblem problem =
                new ApiProblem(null, "Bad Request", 400, null, null, "X", "id", params);
        params.clear();

        assertThat(problem.invalidParams()).containsExactly(new ApiProblem.Param("a", "b"));
        assertThat(
                        new ApiProblem(null, "Not Found", 404, null, null, "X", "id", null)
                                .invalidParams())
                .isNull();
    }

    @Test
    void unreadableBody_becomes400WithACode() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void wrongMethod_stillHasACodeAndRequestId() throws Exception {
        mvc.perform(post("/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Getter
    @RequiredArgsConstructor
    enum TestErrorCode implements ErrorCode {
        THING_NOT_FOUND(
                "TEST_THING_NOT_FOUND", "Không tìm thấy thứ cần tìm", ErrorCategory.NOT_FOUND),
        RULE_BROKEN("TEST_RULE_BROKEN", "Vi phạm luật", ErrorCategory.BUSINESS_RULE),
        INVALID_FILTER("TEST_INVALID_FILTER", "Bộ lọc không hợp lệ", ErrorCategory.VALIDATION);

        private final String code;
        private final String defaultMessage;
        private final ErrorCategory category;
    }

    static class ThingNotFoundException extends ResourceNotFoundException {
        ThingNotFoundException() {
            super(TestErrorCode.THING_NOT_FOUND);
        }
    }

    static class RuleBrokenException extends DomainException {
        RuleBrokenException() {
            super(TestErrorCode.RULE_BROKEN);
        }
    }

    static class InvalidFilterException extends ValidationException {
        InvalidFilterException(List<InvalidParam> params) {
            super(TestErrorCode.INVALID_FILTER, params);
        }
    }

    record NamedRequest(@NotBlank String name) {}

    @RestController
    static class ThrowingController {

        @GetMapping("/not-found")
        void notFound() {
            throw new ThingNotFoundException();
        }

        @GetMapping("/business-rule")
        void businessRule() {
            throw new RuleBrokenException();
        }

        @GetMapping("/invalid-filter")
        void invalidFilter() {
            throw new InvalidFilterException(
                    List.of(
                            new InvalidParam("minPrice", "Giá tối thiểu không được âm"),
                            new InvalidParam("page", "Số trang bắt đầu từ 1")));
        }

        @GetMapping("/concurrent")
        void concurrent() {
            throw new ObjectOptimisticLockingFailureException(Object.class, "id");
        }

        @GetMapping("/boom")
        void boom() {
            throw new IllegalStateException("secret internal detail");
        }

        @GetMapping("/sized")
        int sized(@RequestParam @Min(1) int size) {
            return size;
        }

        @GetMapping("/paged")
        int paged(@RequestParam int page) {
            return page;
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody NamedRequest request) {
            // Bean Validation runs before the method; nothing to do here.
        }
    }
}
