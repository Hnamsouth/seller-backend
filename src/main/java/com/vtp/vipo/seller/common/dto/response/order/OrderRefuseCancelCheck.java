package com.vtp.vipo.seller.common.dto.response.order;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderRefuseCancelCheck {

    private OrderPackageEntity orderPackage;

    private Boolean isBuyerCancel;

    private Boolean isSellerCancel;

}
