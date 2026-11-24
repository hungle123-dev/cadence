package com.cadence.common.web.response;

/** {@code {"data": …}} for one object: every endpoint has the same shape. */
public record UIApiResponse<T>(T data) {

    public static <T> UIApiResponse<T> of(T data) {
        return new UIApiResponse<>(data);
    }
}
