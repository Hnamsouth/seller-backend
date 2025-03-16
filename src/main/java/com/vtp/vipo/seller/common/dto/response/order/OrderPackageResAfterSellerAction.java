package com.vtp.vipo.seller.common.dto.response.order;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Data Transfer Object (DTO) representing the response after a seller performs an action on an order package.
 * This class encapsulates the outcome of the seller's action, including whether it was successful,
 * the reason for failure (if any), and relevant order details.
 *
 *
 * <p><strong>Example JSON Representation:</strong></p>
 * <pre>{@code
 * {
 *     "id": 12345,
 *     "order_code": "ORD-2024-0001",
 *     "success": true,
 *     "reason": null
 * }
 * }</pre>
 *
 * @author AnhDev
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPackageResAfterSellerAction {

    //order_package.id
    Long id;

    //order_package.orderCode
    String orderCode;

    /**
     * Indicates whether the seller's action was successful.
     *
     * <p>If {@code true}, the action (e.g., approval, rejection) was completed successfully.
     * If {@code false}, the action failed.</p>
     */
    Boolean success;

    /**
     * Reason for the failure of the seller's action, if applicable.
     *
     * <p>This field provides additional context or error messages when {@code success} is {@code false}.
     * It helps in understanding why the action did not succeed.</p>
     */
    String reason;

}
