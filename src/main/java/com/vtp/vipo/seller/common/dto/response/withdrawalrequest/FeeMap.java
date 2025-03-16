package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FeeMap {

    String feeName;

    BigDecimal feeValue;

    String columnCode;

    public FeeMap clone(){
        FeeMap cloned = new FeeMap();
        BeanUtils.copyProperties(this, cloned);
        return cloned;
    }
}
