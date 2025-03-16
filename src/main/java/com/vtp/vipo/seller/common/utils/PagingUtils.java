package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Utility class providing methods for handling pagination and sorting.
 * Contains helper methods to validate and create Pageable objects.
 */
public final class PagingUtils {

    private PagingUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates the given Pageable object and returns a valid Pageable if the request is valid.
     *
     * @param pageable The Pageable object containing page information and sorting.
     * @param sortFields A set of valid sort field names.
     * @return A validated Pageable object.
     * @throws VipoInvalidDataRequestException If the pagination or sorting request is invalid.
     */
    public static Pageable validatePageableAndReturnPageable(Pageable pageable, Set<String> sortFields) {
        Sort sort = pageable.getSort();

        // Check if the sorting is empty
        if (ObjectUtils.isEmpty(sort))
            throw new VipoInvalidDataRequestException(BaseExceptionConstant.INVALID_PAGING_REQUEST_DESCRIPTION);

        // Check if all sort fields are valid
        if (!ObjectUtils.isEmpty(sortFields)) {
            sort.get().forEach(order -> {
                if (!sortFields.contains(order.getProperty())) {
                    throw new VipoInvalidDataRequestException(BaseExceptionConstant.INVALID_PAGING_REQUEST_DESCRIPTION);
                }
            });
        }

        // Validate the page number is greater than or equal to 1
        if (pageable.getPageNumber() < 1) {
            throw new VipoInvalidDataRequestException(BaseExceptionConstant.INVALID_PAGING_REQUEST_DESCRIPTION);
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    }

    /**
     * Creates a Pageable object based on provided parameters, using default values if necessary.
     *
     * @param page The page number.
     * @param size The size of the page (number of items per page).
     * @param sortString A string representing the sorting order.
     * @param defaultSort The default sorting order if no specific sort string is provided.
     * @return A Pageable object based on the provided parameters.
     */
    public static Pageable createPageable(
            Integer page, Integer size, String sortString, Sort defaultSort
    ) {
        return createPageable(
                page, size, sortString,
                Constants.DEFAULT_PAGE_NUM, Constants.DEFAULT_PAGE_SIZE, defaultSort,
                Constants.PAGE_SIZE_LIMIT
        );
    }

    /**
     * Creates a Pageable object based on provided parameters, using default values if necessary,
     * and applying the size limit.
     *
     * @param page The page number.
     * @param size The size of the page (number of items per page).
     * @param sortString A string representing the sorting order.
     * @param defaultPageNum The default page number if none is provided.
     * @param defaultPageSize The default size of the page if none is provided.
     * @param defaultSort The default sorting order if no specific sort string is provided.
     * @param sizeLimit The maximum allowed size for the page.
     * @return A Pageable object based on the provided parameters.
     */
    public static Pageable createPageable(
            Integer page, Integer size, String sortString,
            Integer defaultPageNum, Integer defaultPageSize, Sort defaultSort,
            Integer sizeLimit
    ) {

        /* page num */
        if (ObjectUtils.isEmpty(page)) {
            page = defaultPageNum;
        }
        if (ObjectUtils.isEmpty(page)) {
            return null;  // Page number is required.
        }

        /* page size */
        if (ObjectUtils.isEmpty(size)) {
            size = defaultPageSize;
        }
        if (ObjectUtils.isEmpty(size)) {
            return null;  // Page size is required.
        }

        // Ensure the page size does not exceed the size limit
        if (size > sizeLimit) {
            size = sizeLimit;
        }

        /* sorting */
        Sort sort = StringUtils.isEmpty(sortString) ? defaultSort : Sort.by(sortString);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates a {@link Sort} object based on a comma-separated string of sorting parameters.
     * The string should contain alternating field names and directions, e.g., "field1,ASC,field2,DESC".
     * If the input is invalid or empty, it returns an unsorted {@link Sort}.
     *
     * @param sort A comma-separated string representing sorting parameters.
     * @return A {@link Sort} object constructed from the provided string.
     * @throws IllegalArgumentException If the string format is invalid (e.g., mismatched field and direction pairs).
     */
    public static Sort createSortFromString(String sort) {
        // If the sort string is blank or empty, return an unsorted Sort
        if (StringUtils.isBlank(sort) || sort.isEmpty()) {
            return Sort.unsorted();
        }

        // Split the string into an array of parameters
        String[] sortParams = sort.split(",");

        // Ensure the number of parameters is even (field, direction pairs)
        if (sortParams.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid sort parameter");
        }

        // Initialize an array to hold the Sort.Order objects
        Sort.Order[] orders = new Sort.Order[sortParams.length / 2];

        // Iterate through the sort parameters and create Sort.Order objects
        for (int i = 0; i < sortParams.length; i += 2) {
            String property = sortParams[i]; // The field name to sort by
            Sort.Direction direction = Sort.Direction.fromString(sortParams[i + 1]); // The sort direction (ASC/DESC)

            // Create a new Sort.Order and add it to the orders array
            orders[i / 2] = new Sort.Order(direction, property);
        }

        // Return a Sort object with the constructed Sort.Order array
        return Sort.by(orders);
    }

    public static int calculateTotalPage(int pageSize, long totalPage) {
        return (int) Math.ceil((double) totalPage / pageSize);

    }

}