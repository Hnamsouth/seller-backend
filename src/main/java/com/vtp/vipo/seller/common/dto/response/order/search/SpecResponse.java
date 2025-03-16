package com.vtp.vipo.seller.common.dto.response.order.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * Class representing a specification of a product.
 * This class contains details about a specific product's property, such as the property name
 * (e.g., 'Color', 'Size') and the value of that property (e.g., 'Red', 'Large').
 */
public class SpecResponse {

    /**
     * @return The name of the product property (e.g., 'Color', 'Size')
     */
    private String propName;

    /**
     * @return The value of the product property (e.g., 'Red', 'Large')
     */
    private String propValue;

}

