package com.vtp.vipo.seller.common.dto.response.order;

import com.vtp.vipo.seller.common.utils.DateUtils;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Class representing the details of an order that has been refused or canceled.
 * This class contains information about the order, its status, the reason for the refusal or cancellation,
 * the affected SKUs, and any additional notes related to the cancellation.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRefuseCancelInfo {

    /**
     * @return The unique code of the order being canceled or refused
     */
    private String orderCode;

    /**
     * @return The current status of the order (e.g., 'Cancelled', 'Refused')
     */
    private String status;

    /**
     * @return The reason code for the cancellation or refusal of the order (e.g., 'Out of Stock', 'Customer Request')
     */
    private String reasonCode;

    /**
     * @return A list of SKU IDs affected by the cancellation or refusal of the order
     */
    private List<Long> skuIds;

    /**
     * @return An optional note providing additional explanation or details about the cancellation or refusal
     */
    private String reasonNote;

    /**
     * @return the time execute refuse or cancel order
     */
    private Long executedAt;

    private String failedMessage;

    public OrderRefuseCancelInfo(String orderCode, List<Long> skuIds, String reasonCode, String status, String reasonNote) {
        this.orderCode = orderCode;
        this.status = status;
        this.reasonCode = reasonCode;
        this.skuIds = skuIds;
        this.reasonNote = reasonNote;
        this.executedAt = DateUtils.getTimeInSeconds(LocalDateTime.now());
    }
}
