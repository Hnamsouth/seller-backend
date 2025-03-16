package com.vtp.vipo.seller.common.dto.response.order.search;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.enumseller.OrderAction;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Class representing the response for a list of orders.
 * This class holds various details related to an order, including the order ID, customer information,
 * product quantities, payment details, shipment information, and order status.
 * It is used to return detailed order data, including product information and any actions related to the order.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderListResponse {

    /**
     * @return The unique ID of the order
     */
    Long id;

    /**
     * @return The order code (unique identifier for the order)
     */
    String orderCode;

    /**
     * @return The status of the order for byer
     */
    String orderStatus;

    /**
     * @return The customer ID who placed the order
     */
    Long customerId;

    /**
     * @return The name of the customer who placed the order
     */
    String customerName;

    /**
     * @return The avatar or profile image of the customer
     */
    String customerAvatar;

    /**
     * @return The total quantity of products in the order
     */
    Integer quantityProduct;

    /**
     * @return The seller's order status
     */
    SellerOrderStatus sellerOrderStatus;

    /**
     * @return The total payment amount for the order
     */
    BigDecimal totalPayment;

    /**
     * @return The total amount already paid for the order
     */
    BigDecimal totalPaid;

    /**
     * @return The pre-payment amount made for the order
     */
    BigDecimal prePayment;

    /**
     * @return The timestamp of the last update to the order
     */
    Long updatedAt;

    /**
     * @return The timestamp when the order was created
     */
    Long createdAt;

    /**
     * @return The timestamp when the shipment was made
     */
    Long shipmentTime;

    /**
     * @return The shipment tracking code for the order
     */
    String shipmentCode;

    String shipmentMessage;

    /**
     * @return A list of products included in the order package
     */
    List<PackageProductResonse> packageProducts;

    /**
     * @return The total amount of the products in the order (excluding other fees)
     */
    BigDecimal productAmount;

    /**
     * @return A description of the seller's order status
     */
    String sellerOrderStatusDesc;

    /**
     * @return The cancellation note or reason, if the order was canceled
     */
    String cancelNote;

    /**
     * @return The timestamp when the order was paid
     */
    Long paymentTime;

    /**
     * @return The amount of money refunded for the order, if applicable
     */
    BigDecimal returnAmount;

    /**
     * @return The timestamp when the order was successfully delivered
     */
    Long deliverySuccessTime;

    /**
     * @return The timestamp when the order was canceled
     */
    Long cancelTime;

    /**
     * @return The total amount refunded to the customer
     */
    BigDecimal refundAmount;

    /**
     * @return The timestamp when the return was processed
     */
    Long returnTime;

    /**
     * @return A flag indicating if the price of the product was changed during processing
     */
    Integer isChangePrice;

    Integer isPrinted;

    String shipmentStatus;

    /**
     * @return A list of actions performed on the order (e.g., 'Cancel', 'Refund', etc.)
     */
    List<OrderAction> actions;

}
