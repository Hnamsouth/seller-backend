package com.vtp.vipo.seller.common.dto.request.withdrawalrequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportWithdrawalRequestListRequest {

    /**
     * The type of the withdrawal request.
     * be different categories or classifications as defined by {@link WithdrawalRequestType}.
     */
    WithdrawalRequestType withdrawalRequestType;

    /**
     * The status of the withdrawal request.
     * rejected, etc., as defined by {@link WithdrawRequestStatusEnum}.
     */
    List<WithdrawRequestStatusEnum> withdrawalRequestStatus;

    /**
     * The minimum amount for the withdrawal request filter.
     */
    BigDecimal amountFrom;

    /**
     * The maximum amount for the withdrawal request filter.
     */
    BigDecimal amountTo;

    /**
     * The start date for the withdrawal request filter.
     */
    Long startDate;

    /**
     * The end date for the withdrawal request filter.
     */
    Long endDate;

}
