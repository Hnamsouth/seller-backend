package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collection;

@Getter
@Setter
public class WithdrawalPaging extends PagingRs {

    Collection<FeeColumn> feeColumns;

    BigDecimal taxValue;

}
