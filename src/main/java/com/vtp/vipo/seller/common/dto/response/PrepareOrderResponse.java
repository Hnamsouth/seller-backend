package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dto.ActivityDetailsData;
import com.vtp.vipo.seller.common.enumseller.ShippingConnectionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrepareOrderResponse {
    List<PrepareOrderData> data = new ArrayList<>();

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PrepareOrderData extends ActivityDetailsData {
        ShippingConnectionStatus status;

        Long orderId;

        String orderCode;

        String shipmentCode;

        String message;

        String orderStatus;
    }
}
