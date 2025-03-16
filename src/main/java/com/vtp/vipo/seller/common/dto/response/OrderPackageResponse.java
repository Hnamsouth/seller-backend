package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.PaymentMethodConstants;
import com.vtp.vipo.seller.common.constants.ValidatorCommon;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrderPackageResponse {

    private String key;

    private String orderCode;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String productName;

    private String skuImageUrl;

    @JsonIgnore
    private String skuInfo;

    private List<SpecProp> spec;

    private Long quantityProduct;

    private BigDecimal totalPayment;

    private String orderStatus;

    private BigDecimal totalPaid;

    private String paymentMethod;

    private String createdTime;

    private String updatedTime;

    private BigDecimal productAmount;

    private BigDecimal productAmountRMB;

    private BigDecimal prepayment;

    private String productId;

    private Integer productPlatformType;

    private String orderStatusDescription;

    private String orderStatusCode;

    private String cancelNote;

    private String paymentTime;

    private BigDecimal returnAmount;

    private BigDecimal totalPaymentAmount;

    private String logisticsNo;


    public OrderPackageResponse(String orderCode,String receiverName,String receiverPhone,String receiverAddress, String productName,String skuInfo, String skuImageUrl, Long quantityProduct, BigDecimal totalPayment, String orderStatus, BigDecimal totalPaid,Long createdTime, Long updatedTime, BigDecimal productAmount, BigDecimal productAmountRMB, Long paymentTime, String productId, Integer productPlatformType, String orderStatusDescription, String orderStatusCode,String logisticsNo, String cancelNote) {
        this.orderCode = orderCode;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress =receiverAddress;
        this.productName = productName;
        this.skuInfo = skuInfo;
        this.skuImageUrl = skuImageUrl;
        this.quantityProduct = quantityProduct;
        this.totalPayment = totalPayment;
        this.orderStatus = orderStatus;
        this.paymentMethod = PaymentMethodConstants.PAYMENT_IN_ADVANCE;
        this.createdTime = ValidatorCommon.validateTime(createdTime);
        this.updatedTime = ValidatorCommon.validateTime(updatedTime);
        this.productAmount = productAmount;
        this.productAmountRMB = productAmountRMB;
        this.prepayment = totalPaid;
        this.productId = productId;
        this.productPlatformType = productPlatformType;
        this.paymentTime = ValidatorCommon.validateTime(paymentTime);;
        if (ObjectUtils.isEmpty(paymentTime) || paymentTime.equals(0L)) {
            this.totalPaid = new BigDecimal(0);

        } else {
            this.totalPaid = totalPaid;
        }
        this.orderStatusDescription = orderStatusDescription;
        this.orderStatusCode = orderStatusCode;
        this.cancelNote = cancelNote;
        this.logisticsNo = logisticsNo;

        /* Plus reason to some cancel status to orderStatusDescription */
        if (!ObjectUtils.isEmpty(orderStatusCode) && Constants.CANCEL_NOTE_REQUIRED_CANCEL_STATUSES.contains(orderStatusCode) && !ObjectUtils.isEmpty(cancelNote)) {
            this.orderStatusDescription = this.orderStatusDescription + " " + cancelNote;
        }
    }


}
