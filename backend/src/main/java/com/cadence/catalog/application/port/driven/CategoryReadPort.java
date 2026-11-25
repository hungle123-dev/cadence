package com.cadence.catalog.application.port.driven;

import java.util.List;

import com.cadence.catalog.application.dto.CategoryView;

public interface CategoryReadPort {

    /** Every category, in display order. */
    List<CategoryView> findAllOrdered();
}
