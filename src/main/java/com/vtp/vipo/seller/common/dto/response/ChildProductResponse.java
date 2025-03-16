package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildProductResponse extends GetProductResponse{

    private String key;

    private String sku ="";

    private String property;

    public ChildProductResponse(Long id, String image, String name, Long price, int status, int buyCount, Long createTime, Long updateTime, Long hold, int activated, String property) {
        super(id, image, name, price, status, buyCount, createTime, updateTime, hold, activated);
        this.property = property;
    }

}
