package com.cadence.common.web;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.cadence.common.domain.error.CommonErrorCode;
import com.cadence.common.domain.error.DomainException;
import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import com.cadence.common.domain.error.ValidationException;

import lombok.extern.slf4j.Slf4j;

/**
 * The single place that turns exceptions into HTTP responses (architecture 9.4): every error is an
 * RFC 9457 Problem Details body with a stable {@code code} and the {@code requestId}.
 *
 * <p>Extending {@link ResponseEntityExceptionHandler} keeps Spring's own handling of MVC errors
 * (404, 405, ...) and lets us add the code and request id to them. Unexpected exceptions become a
 * generic 500: the stack trace goes to the log, never to the client.
 *
 * <p>The web app branches on {@code code}, never on {@code detail}. Business errors bring their
 * {@link ErrorCode}; bad request input gets {@code VALIDATION_ERROR}; other errors Spring MVC
 * raises get a code derived from the HTTP status ({@code NOT_FOUND}, {@code METHOD_NOT_ALLOWED}),
 * and any 5xx is {@code INTERNAL_ERROR}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Name of the Problem Details property holding the stable error code. */
    public static final String CODE = "code";

    static final String INVALID_PARAMS = "invalidParams";

    /**
     * A value that cannot even be converted ("abc" for a number): Spring's text is not for users.
     */
    static final String UNREADABLE_VALUE = "Giá trị không hợp lệ";

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleBusiness(DomainException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        statusFor(errorCode.getCategory()), ex.getMessage());
        problem.setProperty(CODE, errorCode.getCode());
        if (ex instanceof ValidationException invalid && !invalid.getInvalidParams().isEmpty()) {
            problem.setProperty(
                    INVALID_PARAMS,
                    invalid.getInvalidParams().stream()
                            .map(param -> new ApiProblem.Param(param.name(), param.reason()))
                            .toList());
        }
        return withRequestId(problem);
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

    /**
     * {@code @Version} (AuditEntity): someone else saved the same row first. Expected under
     * concurrency, so 409 with a code the client can act on (reload), not a 500.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        log.info("Concurrent update: {}", ex.getMessage());
        ErrorCode conflict = CommonErrorCode.CONCURRENT_UPDATE;
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        statusFor(conflict.getCategory()), conflict.getDefaultMessage());
        problem.setProperty(CODE, conflict.getCode());
        return withRequestId(problem);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex); // the MDC adds the requestId
        ErrorCode internal = CommonErrorCode.INTERNAL_ERROR;
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR, internal.getDefaultMessage());
        problem.setProperty(CODE, internal.getCode());
        return withRequestId(problem);
    }

    // ---------- Request validation: the same answer for every module ----------

    /** Bean Validation on a request record ({@code @Valid @ParameterObject} or a JSON body). */
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<ApiProblem.Param> params =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> new ApiProblem.Param(error.getField(), reasonFor(error)))
                        .toList();
        return validationProblem(ex, params, headers, request);
    }

    /** Constraints written on controller parameters ({@code @Min} on a {@code @RequestParam}). */
    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<ApiProblem.Param> params =
                ex.getParameterValidationResults().stream()
                        .flatMap(result ->
                                result.getResolvableErrors().stream()
                                        .map(error ->
                                                new ApiProblem.Param(
                                                        parameterName(
                                                                result.getMethodParameter()
                                                                        .getParameterName()),
                                                        messageOf(error))))
                        .toList();
        return validationProblem(ex, params, headers, request);
    }

    /**
     * A {@code @RequestParam} or {@code @PathVariable} that cannot be converted ({@code page=abc}).
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return validationProblem(
                ex,
                List.of(
                        new ApiProblem.Param(
                                parameterName(ex.getPropertyName()), UNREADABLE_VALUE)),
                headers,
                request);
    }

    private @Nullable ResponseEntity<Object> validationProblem(
            Exception ex, List<ApiProblem.Param> params, HttpHeaders headers, WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.VALIDATION_ERROR;
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        statusFor(errorCode.getCategory()), errorCode.getDefaultMessage());
        problem.setProperty(CODE, errorCode.getCode());
        problem.setProperty(INVALID_PARAMS, params);
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static String parameterName(@Nullable String name) {
        return name != null ? name : "request";
    }

    private static String reasonFor(FieldError error) {
        return error.isBindingFailure() ? UNREADABLE_VALUE : messageOf(error);
    }

    private static String messageOf(MessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return message != null ? message : UNREADABLE_VALUE;
    }

    // ---------- Every error Spring MVC handles itself ----------

    /** Every error Spring MVC handles itself passes through here: add the code and request id. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        if (body instanceof ProblemDetail problem) {
            withDefaultCode(problem, statusCode);
            withRequestId(problem);
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    /** Keeps a code set by a handler above; otherwise derives one from the HTTP status. */
    private static void withDefaultCode(ProblemDetail problem, HttpStatusCode statusCode) {
        var properties = problem.getProperties();
        if (properties != null && properties.containsKey(CODE)) {
            return;
        }
        problem.setProperty(CODE, codeFor(statusCode));
    }

    static String codeFor(HttpStatusCode statusCode) {
        if (statusCode.is5xxServerError()) {
            return CommonErrorCode.INTERNAL_ERROR.getCode();
        }
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status != null ? status.name() : "HTTP_" + statusCode.value();
    }

    private static ProblemDetail withRequestId(ProblemDetail problem) {
        problem.setProperty(RequestIdFilter.MDC_KEY, MDC.get(RequestIdFilter.MDC_KEY));
        return problem;
    }
}
