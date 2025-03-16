package com.vtp.vipo.seller.common.dto.request.base;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.constants.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LazBaoBaseRequest {

    private String language = Constants.LANGUAGE;

    private String currency = Constants.CURRENCY;

}
