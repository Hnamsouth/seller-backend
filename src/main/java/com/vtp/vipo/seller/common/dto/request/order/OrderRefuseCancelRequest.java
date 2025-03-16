package com.vtp.vipo.seller.common.dto.request.order;

import com.vtp.vipo.seller.common.enumseller.OrderRefuseCancelType;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Class representing a request to refuse or cancel an order.
 * This class contains details about the order to be canceled or refused, including the order ID,
 * the reason for the cancellation, SKU IDs related to the order, and an optional reason note.
 */
public class OrderRefuseCancelRequest {

    /**
     * @return The unique ID of the order to be canceled or refused
     * @throws IllegalArgumentException if the order ID is blank
     */
    String orderId;

    /**
     * @return The reason code for canceling or refusing the order
     * @throws IllegalArgumentException if the reason code is blank
     */
    OrderRefuseCancelType reasonCode;

    /**
     * @return A list of SKU IDs associated with the order that may be affected by the cancellation
     */
    List<Long> skuIds;

    /**
     * @return An optional note or additional explanation for the reason behind the order refusal or cancellation
     */
    String reasonNote;

}

