package com.vtp.vipo.seller.common.dto.request.order;

import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import lombok.Getter;

@Getter
public class OrderFilterRequest {

    OrderFilterTab tabCode;

    SellerOrderStatus status;

    String orderCode;

    String buyerName;

    String productName;

    String shipmentCode;

    Long startDate;

    Long endDate;

    int pageSize;

    int pageNum;

}
