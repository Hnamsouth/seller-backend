package com.vtp.vipo.seller.common.dto.response.order;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LogisticsTrackInfoVO {

    @NotBlank
    String context;

    String status; //lazbao logistic status

    @NotBlank
    String statusDesc; //lazbao logistic status description

    long time; //timestamp in seconds

}
