package com.vtp.vipo.seller.common.dto.request;

import com.vtp.vipo.seller.common.dto.request.base.LazBaoBaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LazbaoPagingRequest extends LazBaoBaseRequest {

    @ApiModelProperty(example = "1")
    private int pageNo = 1;

    @ApiModelProperty(example = "10")
    private int pageSize = 10;

    @ApiModelProperty(example = "10", value="{10} - 1688 , {21} - Taobao")
    private int platformType = 10;

}
