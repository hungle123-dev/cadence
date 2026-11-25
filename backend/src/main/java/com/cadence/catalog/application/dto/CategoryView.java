package com.cadence.catalog.application.dto;

import java.util.UUID;

/** A category, in display order. Categories are flat (no parent) in the MVP. */
public record CategoryView(UUID id, String slug, String name) {}
