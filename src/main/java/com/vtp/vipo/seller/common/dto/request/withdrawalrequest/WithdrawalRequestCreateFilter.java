package com.vtp.vipo.seller.common.dto.request.withdrawalrequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class WithdrawalRequestCreateFilter {

    /**
     * The code associated with the shipment.
     */
    String shippingCode;

    /**
     * The code associated with the order.
     */
    String orderCode;

    /**
     * The name of the buyer.
     */
    String buyerName;

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
