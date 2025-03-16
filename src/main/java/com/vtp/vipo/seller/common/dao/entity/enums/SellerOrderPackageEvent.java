package com.vtp.vipo.seller.common.dao.entity.enums;

/**
 * Represents the events that can trigger transitions in the seller's order state machine.
 * <p>
 * Each event corresponds to an action or occurrence that affects the {@link SellerOrderStatus}
 * of an {@link com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity}.
 * </p>
 *
 * @see SellerOrderStatus
 * @see com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity
 */
public enum SellerOrderPackageEvent {

    /**
     * Event triggered when the seller notifies that the payment for the order has been received.
     * <p>
     * Transitions the order status from {@code WAITING_FOR_PAYMENT} to {@code APPROVED}.
     * </p>
     */
    SELLER_NOTIFY_PAYMENT,

    /**
     * Event triggered when the seller approves the order package.
     * <p>
     * Transitions the order status to {@code APPROVED}, indicating that the seller has accepted the order.
     * </p>
     */
    APPROVED,

    /**
     * Event triggered when the seller ships the order.
     * <p>
     * Transitions the order status from {@code ORDER_PREPARED} to {@code ORDER_SHIPMENT_CONNECTION_SUCCESS}.
     * </p>
     */
    SHIP,

    /**
     * Event triggered when the order has been delivered to the buyer.
     * <p>
     * Transitions the order status from {@code ORDER_IN_TRANSIT} to {@code ORDER_DELIVERED_TO_SHIPPING}.
     * </p>
     */
    DELIVER,

    /**
     * Event triggered when the order is cancelled by the buyer or seller.
     * <p>
     * Transitions the order status to {@code ORDER_CANCELLED_BY_SELLER} or {@code SELLER_CANCELLED_ORDER},
     * depending on who initiates the cancellation.
     * </p>
     * <p>
     * Vietnamese Translation: "Hủy"
     * </p>
     */
    CANCEL,

    /**
     * Event triggered when the seller rejects the order.
     * <p>
     * Transitions the order status to {@code SELLER_REJECTED_ORDER}, indicating that the seller has declined the order.
     * </p>
     * <p>
     * Vietnamese Translation: "Từ chối"
     * </p>
     */
    REJECT,

    /**
     * Event triggered when the goods for the order have been prepared by the seller.
     * <p>
     * Transitions the order status from {@code WAITING_FOR_ORDER_PREPARATION} to {@code ORDER_PREPARED}.
     * </p>
     */
    PREPARE_GOODS
}