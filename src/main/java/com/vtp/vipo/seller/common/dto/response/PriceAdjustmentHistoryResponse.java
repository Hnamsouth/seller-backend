package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dto.ActivityDetailsData;
import com.vtp.vipo.seller.common.enumseller.PriceAdjustmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PriceAdjustmentHistoryResponse {
    Long id;

    Long orderId;

    PriceAdjustmentType type;

    String createdBy;

    LocalDateTime createdAt;

    String refCode;

    @JsonIgnore
    ActivityDetailsData detailsData;
}
