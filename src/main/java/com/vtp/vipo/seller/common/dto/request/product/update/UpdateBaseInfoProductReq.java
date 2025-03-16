package com.vtp.vipo.seller.common.dto.request.product.update;

import com.vtp.vipo.seller.common.dto.request.product.BaseProductInfo;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Author: hieuhm12
 * Date: 9/18/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBaseInfoProductReq extends BaseProductInfo {
    @NotNull
    Long id;
}
