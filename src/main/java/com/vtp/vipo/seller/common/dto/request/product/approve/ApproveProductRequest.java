package com.vtp.vipo.seller.common.dto.request.product.approve;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApproveProductRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Integer wantedStatus;

}
