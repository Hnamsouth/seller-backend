package com.vtp.vipo.seller.common.dto.response.product.detail;

import com.vtp.vipo.seller.common.dto.request.product.ProductAttributesInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: hieuhm12
 * Date: 9/16/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductAttributesDetailResponse extends ProductAttributesInfo {
    Long id;

    boolean allowToRemoveAttribute= true;

    String blockRemovingAttributeMsg;

    boolean allowToRenameAttribute = true;

    String blockRenamingAttributeMsg;

    boolean allowToAddNewClassify = true;

    String blockAddingNewClassifyMsg;
}
