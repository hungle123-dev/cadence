package com.cadence.catalog.application.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The configured price bands of the PLP filter (FR-CAT-02, D-31), lowest first. Built by the
 * infrastructure from configuration, so changing the bands needs no code change.
 */
public record PriceBands(List<PriceBand> bands) {

    public PriceBands {
        bands = List.copyOf(bands);
        if (bands.isEmpty()) {
            throw new IllegalArgumentException("at least one price band is needed");
        }
    }

    /** Bands from boundaries: {@code [1M, 2M]} gives under 1M, 1M to 2M, 2M and above. */
    public static PriceBands fromBoundaries(List<Long> boundaries) {
        var bands = new ArrayList<PriceBand>();
        long min = 0;
        for (long boundary : boundaries) {
            bands.add(new PriceBand(min, boundary));
            min = boundary;
        }
        bands.add(new PriceBand(min, null));
        return new PriceBands(bands);
    }

    public boolean contains(PriceBand band) {
        return bands.contains(band);
    }
}
