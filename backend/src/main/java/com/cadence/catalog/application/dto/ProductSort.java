package com.cadence.catalog.application.dto;

/** PLP order (AC 2.6, 2.7). Prices are effective prices (R-PRICE); ties are broken by colorway id (A3). */
public enum ProductSort {
    NEWEST, PRICE_ASC, PRICE_DESC
}
