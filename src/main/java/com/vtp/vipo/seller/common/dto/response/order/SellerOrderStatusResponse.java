package com.vtp.vipo.seller.common.dto.response.order;

import com.vtp.vipo.seller.common.dto.response.OrderStatusResponse;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SellerOrderStatusResponse {

    private String code;

    private String name;

    private Long totalOrder;

    private List<SellerOrderStatusResponse> childStatus;

    private boolean isAccessible = true;

    public SellerOrderStatusResponse(String code, String name, Long totalOrder) {
        this.code = code;
        this.name = name;
        this.totalOrder = totalOrder;
    }
}
