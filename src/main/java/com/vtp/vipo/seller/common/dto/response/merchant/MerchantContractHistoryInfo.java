package com.vtp.vipo.seller.common.dto.response.merchant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.MerchantLogActionEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantContractHistoryInfo {

    Long time;

    String updateBy;

    MerchantLogActionEnum updateActionType;

    String updateActionMessage;

    List<MerchantLogAttributeChangeInfo> changes;

}
