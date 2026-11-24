package com.cadence.common.web;

import com.cadence.common.domain.error.DomainException;
import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

/**
 * The single place that turns exceptions into HTTP responses. Body is RFC 9457 Problem Details plus a stable
 * {@code code} the frontend branches on (architecture 9.4).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException ex) {
        return problem(ex.getErrorCode(), ex.getMessage());
    }

    /** {@code @Version} clash: someone saved first. 409, not 500. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        log.info("Concurrent update rejected: {}", ex.getMessage());
        return problem(CommonErrorCode.CONCURRENT_UPDATE, CommonErrorCode.CONCURRENT_UPDATE.getDefaultMessage());
    }

    /** Anything else: stack trace to the log, generic text to the client. */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(CommonErrorCode.INTERNAL_ERROR, CommonErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> invalid = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::toInvalidParam)
                .toList();
        return validationResponse(invalid);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> invalid = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> Map.of(
                                "name", result.getMethodParameter().getParameterName() == null
                                        ? "?" : result.getMethodParameter().getParameterName(),
                                "reason", String.valueOf(error.getDefaultMessage()))))
                .toList();
        return validationResponse(invalid);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = problem(CommonErrorCode.MALFORMED_REQUEST, CommonErrorCode.MALFORMED_REQUEST.getDefaultMessage());
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    /**
     * Errors Spring handles itself (404 for an unknown path, 405, 415…) still get a {@code code},
     * so the frontend never sees an error without one.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        // Spring builds the ProblemDetail inside super when body is null, so add the code afterwards.
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem
                && (problem.getProperties() == null || !problem.getProperties().containsKey("code"))) {
            problem.setProperty("code", statusCode.value() == HttpStatus.NOT_FOUND.value()
                    ? CommonErrorCode.NOT_FOUND.getCode()
                    : "HTTP_" + statusCode.value());
        }
        return response;
    }

    /** The only place where a category becomes an HTTP status. */
    static HttpStatus statusFor(ErrorCategory category) {
        return switch (category) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_CONTENT;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static ProblemDetail problem(ErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(statusFor(code.getCategory()), detail);
        problem.setProperty("code", code.getCode());
        return problem;
    }

    private static ResponseEntity<Object> validationResponse(List<Map<String, String>> invalidParams) {
        ProblemDetail body = problem(CommonErrorCode.VALIDATION_ERROR, CommonErrorCode.VALIDATION_ERROR.getDefaultMessage());
        body.setProperty("invalidParams", invalidParams);
        return ResponseEntity.badRequest().body(body);
    }

    private static Map<String, String> toInvalidParam(FieldError error) {
        return Map.of("name", error.getField(), "reason", String.valueOf(error.getDefaultMessage()));
    }
}
