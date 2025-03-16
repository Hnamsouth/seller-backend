package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Collection;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WithdrawalRequestDetailResponse {

    String id;

    /**
     * The timestamp when the withdrawal request was created.
     *
     * This field represents the creation time of the withdrawal request.
     * The date is represented as a timestamp in seconds.
     */
    Long createAt;

    /**
     * The information of the account initiating the withdrawal.
     */
    String accountInfo;

    /**
     * The status of the withdrawal.
     *
     * This field represents the current status of the {@link WithdrawRequestStatusEnum}.
     */
    WithdrawRequestStatusEnum withdrawalRequestStatus;

    /**
     * A description of the withdrawal status.
     *
     * This field provides additional information or context about the current status of the withdrawal.
     */
    String withdrawalRequestStatusDesc;

    /**
     * A collection of items included in the withdrawal request.
     *
     * This field holds a list of {@link WithdrawalRequestItem} objects, each representing an individual item in the withdrawal request.
     */
    Collection<WithdrawalRequestItem> withdrawalRequestItems;

    Collection<FeeColumn> feeColumns;

    /**
     * The tax amount.
     */
    BigDecimal tax;

    BigDecimal taxValue;

    /**
     * The total amount requested for withdrawal.
     *
     * This field indicates the overall amount of money requested for withdrawal.
     */
    BigDecimal totalWithdrawal;

    String reason;

    Boolean reCreatable;

}
