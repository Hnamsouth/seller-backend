package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class WithdrawalRequestItem {

    /**
     * Num of row
     * */
    Integer stt;

    /**
     * The unique identifier for the order package.
     *
     * This field contains the ID of the order package.
     */
    Long orderPackageId;

    /**
     * The code associated with the order package.
     *
     * This field contains a unique code to identify the order package.
     */
    String orderPackageCode;

    /**
     * The date of successful delivery.
     *
     * This field represents the date when the order package was successfully delivered.
     * The date is represented as a timestamp in milliseconds.
     */
    Long successDeliveryDate;

    /**
     * The time the withdrawal was made.
     */
    Long withdrawalTime;

    /**
     * The shipping code.
     *
     * This field contains the code provided by the shipping service for tracking the package.
     */
    String shippingCode;

    /**
     * The name of the buyer.
     *
     * This field contains the name of the individual or entity who made the purchase.
     */
    String buyerName;

    /**
     * The quantity of items in the order package.
     *
     * This field specifies the total number of items included in the order package.
     */
    Long quantity;

    /**
     * The transaction code for the prepayment.
     *
     * This field contains the transaction code associated with the prepayment made by the buyer.
     */
    String prepaymentTransactionCode;

    /**
     * The amount of prepayment.
     *
     * This field specifies the amount of money pre-paid by the buyer for the order package.
     */
    BigDecimal prepayment;

    /**
     * The amount for cash on delivery (COD).
     */
    BigDecimal codAmount;

    /**
     * The amount of order = prepayment + codAmount - adjustmentPrice
     */
    BigDecimal orderAmount;

    /**
     * A map containing various fees associated with the order package.
     *
     * This field stores different types of fees as key-value pairs, where the key is the fee type and the value is the fee amount.
     */
    Collection<FeeMap> platformFees;

    /**
     * The adjusted price.
     */
    BigDecimal adjustmentPrice;

    /**
     * The estimated profit from the order package.
     *
     * This field calculates the projected profit based on the order and associated costs.
     */
    BigDecimal estimatedProfit;

    Boolean reCreate;

}
