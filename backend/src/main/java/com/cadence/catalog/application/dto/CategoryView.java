package com.cadence.catalog.application.dto;
import java.util.UUID;

public record CategoryView(UUID id, UUID parentId, String slug, String name, int depth, int position) {
}