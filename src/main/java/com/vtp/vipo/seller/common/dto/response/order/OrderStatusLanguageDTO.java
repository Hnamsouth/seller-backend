package com.vtp.vipo.seller.common.dto.response.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 *
 * This class is used to transfer language-specific information of an order status between different layers of the application.
 * It includes details such as the order status ID, the language code, the localized name, and the localized description.
 */
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusLanguageDTO {

    /**
     * The ID of the associated order status.
     * This links the language-specific information to a specific order status.
     */
    Long orderStatusId;

    /**
     * The language code representing the language for this specific order status translation.
     * Example: "en" for English, "vi" for Vietnamese.
     */
    String language;

    /**
     * The localized name of the order status in the specified language.
     */
    String name;

    /**
     * The localized description of the order status in the specified language.
     */
    String description;

}