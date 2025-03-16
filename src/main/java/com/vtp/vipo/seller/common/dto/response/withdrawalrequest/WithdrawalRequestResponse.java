package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestAction;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WithdrawalRequestResponse {

    String id;

    Long date;

    WithdrawalRequestType withdrawalRequestType;

    String withdrawalRequestTypeDesc;

    String accountInfo;

    WithdrawRequestStatusEnum withdrawalRequestStatus;

    String withdrawalRequestStatusDesc;

    String reason;

    BigDecimal amount;

    List<WithdrawRequestAction> actions;

}
