package com.vtp.vipo.seller.common.dto.response.product.detail;

import com.vtp.vipo.seller.common.dto.request.product.ManageProductCodeInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Author: hieuhm12
 * Date: 9/16/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManageProductCodeDetailResponse extends ManageProductCodeInfo {
    Long id;
}
