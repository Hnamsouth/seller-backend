package com.vtp.vipo.seller.common.dto.request.merchant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dto.request.MerchantRequest;
import lombok.Getter;
import lombok.Setter;

/* VIPO-3903: Upload E-Contract: Merchant Change request V2  */
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantRequestV2 extends MerchantRequest {

    Long countryId;

}
