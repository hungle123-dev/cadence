package com.cadence.catalog.domain.model;

/** How the product runs, for the size advice on the PDP (FR-CAT-05). */
public enum Fit {
    /** Runs small: advise half a size up. */
    SMALL,
    TRUE_TO_SIZE,
    /** Runs large: advise half a size down. */
    LARGE
}
