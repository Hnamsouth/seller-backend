package com.vtp.vipo.seller.common.dto.response.merchant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.MerchantContractType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantContractResponse {

    MerchantContractType contractType;

    String contractUrl;

    String partyB;

    MerchantIdInfo idInfo;

    MerchantBankAccountResponse bankAccount;

    String businessNumber;

    String businessRepresent;

    String businessPosition;

    String businessContactPhone;

    String businessContactEmail;

}
