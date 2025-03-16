package com.vtp.vipo.seller.common.dto.response.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object (DTO) for {@link com.vtp.vipo.order.common.dao.entity.OrderStatusEntity}.
 * This class is used to transfer order status information between different layers of the application.
 * It includes details such as the status ID, root code, name, description, status code, and a map of language-specific details.
 */
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusDTO {

    /**
     * Unique identifier of the order status.
     */
    Long id;

    /**
     * Root code representing the base status of the order.
     */
    String rootCode;

    /**
     * Name of the order status.
     */
    String name;

    /**
     * Description providing additional details about the order status.
     */
    String description;

    /**
     * Status code representing the current status of the order.
     */
    String statusCode;

    /**
     * A map containing language-specific details of the order status.
     * Key represents the language code (e.g., "en", "vi"), and the value is an instance of {@link OrderStatusLanguageDTO}
     * that holds language-specific information like localized name or description.
     * <p>
     * This map is initialized to an empty {@link HashMap} by default.
     */
    @Builder.Default
    Map<String, OrderStatusLanguageDTO> languageDTOMap = new HashMap<>();

}
