package com.cadence.catalog.application.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.common.application.Query;
import com.cadence.common.domain.error.InvalidParam;

/**
 * Filters, sort and page of a product list (FR-CAT-01…03). The facets use the same query and ignore
 * sort and page. The constructor rejects every wrong value at once; an absent sort means FEATURED.
 *
 * @param category the category page being shown ({@code /danh-muc/{slug}}); null on the all
 *     products page
 * @param categories the category filter; only allowed when {@code category} is null (FR-CAT-02)
 * @param prices price bands as sent, {@code "min-max"} or {@code "min-"}; each must also be one of
 *     the configured bands, which the service checks
 */
public record ProductListQuery(
        @Nullable String category,
        List<String> categories,
        List<Gender> genders,
        List<Sport> sports,
        List<ColorFamily> colors,
        List<String> sizes,
        List<String> prices,
        boolean onSaleOnly,
        ProductSort sort,
        int page)
        implements Query {

    /** FR-CAT-01: twelve products per "Xem thêm". */
    public static final int PAGE_SIZE = 12;

    /** Shoe sizes (39, 42.5), kids sizes (4, 5), apparel sizes (S, XL), one size (OS). */
    private static final Pattern SIZE = Pattern.compile("\\d{1,2}(\\.5)?|XS|S|M|L|XL|XXL|OS");

    public ProductListQuery {
        // A value sent twice filters the same as once: keep each value once, in the order sent.
        categories = List.copyOf(new LinkedHashSet<>(categories));
        genders = List.copyOf(new LinkedHashSet<>(genders));
        sports = List.copyOf(new LinkedHashSet<>(sports));
        colors = List.copyOf(new LinkedHashSet<>(colors));
        sizes = List.copyOf(new LinkedHashSet<>(sizes));
        prices = List.copyOf(new LinkedHashSet<>(prices));
        sort = sort == null ? ProductSort.FEATURED : sort; // FR-CAT-03 default

        // Names are the HTTP parameter names, because the client reads them.
        var errors = new ArrayList<InvalidParam>();
        if (page < 1) {
            errors.add(new InvalidParam("page", "Số trang bắt đầu từ 1"));
        }
        if (category != null && !categories.isEmpty()) {
            errors.add(
                    new InvalidParam(
                            "categories", "Trang danh mục không lọc thêm theo danh mục khác"));
        }
        for (String price : prices) {
            if (PriceBand.parse(price) == null) {
                errors.add(new InvalidParam("price", "Khoảng giá không hợp lệ: " + price));
            }
        }
        for (String size : sizes) {
            if (!SIZE.matcher(size).matches()) {
                errors.add(new InvalidParam("size", "Size không hợp lệ: " + size));
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidProductFilterException(errors);
        }
    }

    /** The price bands; every text already parsed in the constructor. */
    public List<PriceBand> priceBands() {
        return prices.stream().map(PriceBand::parse).toList();
    }

    /** Rows to skip; long so a huge page number cannot overflow. */
    public long offset() {
        return (long) (page - 1) * PAGE_SIZE;
    }
}
