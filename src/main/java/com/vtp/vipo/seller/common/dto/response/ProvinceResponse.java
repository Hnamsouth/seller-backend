package com.vtp.vipo.seller.common.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ProvinceResponse {

    private Long id;

    private String name;

    private String code;

}
