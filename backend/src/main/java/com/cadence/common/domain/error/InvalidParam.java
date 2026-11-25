package com.cadence.common.domain.error;

/** One wrong input: its name as the client sent it, and a reason the customer can read. */
public record InvalidParam(String name, String reason) {}
