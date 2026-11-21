package com.cadence.common.web;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.cadence.common.domain.error.ConflictException;
import com.cadence.common.domain.error.DomainException;
import com.cadence.common.domain.error.NotFoundException;
import com.cadence.common.domain.error.ValidationException;

/**
 * ADR-0006: every error is an RFC 9457 Problem Details body carrying the correlation id.
 *
 * <p>Extending {@link ResponseEntityExceptionHandler} keeps Spring's own handling of MVC errors
 * (404, 400, 405, ...) and lets us add the correlation id to them. Unexpected exceptions become a
 * generic 500: the stack trace goes to the log, never to the client.
 *
 * <p>Every problem carries a stable {@code code} (section 7.5, A8): the web app branches on it, never on
 * {@code detail}. Business errors set their own code (for example {@code CATALOG_PRODUCT_NOT_FOUND});
 * errors Spring MVC raises itself get one derived from the HTTP status ({@code BAD_REQUEST},
 * {@code NOT_FOUND}, {@code METHOD_NOT_ALLOWED}...), and a 500 is {@code INTERNAL_ERROR}.
 *
 * <p>Business errors ({@link DomainException}) know no HTTP: the status comes from their kind
 * (not found 404, validation 400, conflict 409, any other business rule 422). One generic mapping here
 * instead of one handler per module (template section 3), because the kind already says everything.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Name of the Problem Details property holding the stable error code. */
    public static final String CODE = "code";

    static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    static final String INVALID_PARAMS = "invalidParams";

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleBusiness(DomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(statusFor(ex), ex.getMessage());
        problem.setProperty(CODE, ex.code());
        if (ex instanceof ValidationException invalid && !invalid.invalidParams().isEmpty()) {
            List<ApiProblem.Param> params = invalid.invalidParams().stream()
                    .map(param -> new ApiProblem.Param(param.name(), param.reason())).toList();
            problem.setProperty(INVALID_PARAMS, params);
        }
        return withCorrelationId(problem);
    }

    static HttpStatus statusFor(DomainException ex) {
        return switch (ex) {
            case NotFoundException _ -> HttpStatus.NOT_FOUND;
            case ValidationException _ -> HttpStatus.BAD_REQUEST;
            case ConflictException _ -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex); // log có correlationId nhờ MDC
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra. Vui lòng thử lại sau.");
        problem.setProperty(CODE, INTERNAL_ERROR);
        return withCorrelationId(problem);
    }

    /** Every error Spring MVC handles itself passes through here: add the correlation id. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problem) {
            withDefaultCode(problem, statusCode);
            withCorrelationId(problem);
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    /** Keeps a code set by a business handler; otherwise derives one from the HTTP status. */
    private static void withDefaultCode(ProblemDetail problem, HttpStatusCode statusCode) {
        if (problem.getProperties() != null && problem.getProperties().containsKey(CODE)) {
            return;
        }
        problem.setProperty(CODE, codeFor(statusCode));
    }

    static String codeFor(HttpStatusCode statusCode) {
        if (statusCode.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return INTERNAL_ERROR;
        }
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status != null ? status.name() : "HTTP_" + statusCode.value();
    }

    private static ProblemDetail withCorrelationId(ProblemDetail problem) {
        problem.setProperty(CorrelationIdFilter.MDC_KEY, MDC.get(CorrelationIdFilter.MDC_KEY));
        return problem;
    }
}
