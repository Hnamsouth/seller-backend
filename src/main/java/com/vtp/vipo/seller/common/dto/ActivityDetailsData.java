package com.vtp.vipo.seller.common.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vtp.vipo.seller.common.dto.response.PrepareOrderResponse;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PriceAdjustmentTotalData.class),
        @JsonSubTypes.Type(value = PriceAdjustmentSkuData.class),
        @JsonSubTypes.Type(value = PrepareOrderBeforeShipmentData.class),
        @JsonSubTypes.Type(value = PrepareOrderResponse.PrepareOrderData.class),
        @JsonSubTypes.Type(value = PrintLabelData.class),
        // và các subclass khác
})
public abstract class ActivityDetailsData {

}
