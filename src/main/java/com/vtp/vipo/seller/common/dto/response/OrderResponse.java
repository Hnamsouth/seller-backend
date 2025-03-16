package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.constants.PaymentMethodConstants;
import com.vtp.vipo.seller.common.constants.ValidatorCommon;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrderResponse {

    private String key;

    private Long orderId;

    private String orderCode;

    private String paymentMethod;

    private BigDecimal totalPaymentAmount;

    private String createdTime;

    private String phone;

    private String buyerName;

    private String buyerAddress;

    private BigDecimal prepayment;

    private List<OrderPackageResponse> children;

    public OrderResponse(Long orderId, String orderCode, BigDecimal totalPaymentAmount, Long createdTime, String phone,String buyerName,String buyerAddress) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.totalPaymentAmount = totalPaymentAmount;
        this.paymentMethod = PaymentMethodConstants.PAYMENT_IN_ADVANCE;
        this.createdTime = ValidatorCommon.validateTime(createdTime);
        this.phone = phone;
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
    }
}
