package com.vtp.vipo.seller.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusResponse {

    private String code;

    private String name;

    private Long totalOrder;

}
