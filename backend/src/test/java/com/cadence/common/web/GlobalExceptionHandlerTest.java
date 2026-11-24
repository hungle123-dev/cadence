package com.cadence.common.web;

import com.cadence.common.domain.error.DomainException;
import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import com.cadence.common.domain.error.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundDomainError_becomes404WithItsCode() throws Exception {
        mvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEST_THING_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Không tìm thấy thứ cần tìm"));
    }

    @Test
    void businessRuleError_becomes422() throws Exception {
        mvc.perform(get("/business-rule"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("TEST_RULE_BROKEN"));
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
                .andExpect(jsonPath("$.detail").value(CommonErrorCode.INTERNAL_ERROR.getDefaultMessage()));
    }

    @Test
    void invalidBody_becomes400WithTheInvalidFields() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.invalidParams[0].name").value("name"));
    }

    @Test
    void unreadableBody_becomes400WithACode() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void unknownPath_stillHasACode() throws Exception {
        mvc.perform(post("/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("HTTP_405"));
    }

    enum TestErrorCode implements ErrorCode {
        THING_NOT_FOUND("TEST_THING_NOT_FOUND", "Không tìm thấy thứ cần tìm", ErrorCategory.NOT_FOUND),
        RULE_BROKEN("TEST_RULE_BROKEN", "Vi phạm luật", ErrorCategory.BUSINESS_RULE);

        private final String code;
        private final String defaultMessage;
        private final ErrorCategory category;

        TestErrorCode(String code, String defaultMessage, ErrorCategory category) {
            this.code = code;
            this.defaultMessage = defaultMessage;
            this.category = category;
        }

        public String getCode() { return code; }
        public String getDefaultMessage() { return defaultMessage; }
        public ErrorCategory getCategory() { return category; }
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

    record NamedRequest(@NotBlank String name) {
    }

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

        @GetMapping("/concurrent")
        void concurrent() {
            throw new ObjectOptimisticLockingFailureException(Object.class, "id");
        }

        @GetMapping("/boom")
        void boom() {
            throw new IllegalStateException("secret internal detail");
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody NamedRequest request) {
        }
    }
}
