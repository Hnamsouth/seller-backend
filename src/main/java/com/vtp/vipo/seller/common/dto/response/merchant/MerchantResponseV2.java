package com.vtp.vipo.seller.common.dto.response.merchant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.ContractStatus;
import com.vtp.vipo.seller.common.dto.response.MerchantResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantResponseV2 extends MerchantResponse {

    private Boolean isCountryEditable;

    private ContractStatus contractStatus;

    private MerchantContractResponse contract;

    private Long countryId;

}
