package com.vtp.vipo.seller.common.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PriceAdjustmentMetadata.class),
        // và các subclass khác
})
public abstract class ActivityDetailsMetadata {

}
