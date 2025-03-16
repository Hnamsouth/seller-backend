package com.vtp.vipo.seller.common.dto.request.withdrawalrequest;

import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WithdrawalRequestFilter {

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

    /**
     * The page number for pagination.
     */
    @NotNull(message = "pageNum is required")
    Integer pageNum;

    /**
     * The page size for pagination.
     */
    @NotNull(message = "pageSize is required")
    Integer pageSize;


}
