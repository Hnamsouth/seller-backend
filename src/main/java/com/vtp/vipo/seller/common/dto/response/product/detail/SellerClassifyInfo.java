package com.vtp.vipo.seller.common.dto.response.product.detail;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for SellerClassifyEntity
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerClassifyInfo {

    Long id;

    String tempId;    //if it is new seller classify, require fe generates an unique code and paste to here

    String name;

    String image;

    @Builder.Default
    List<Long> skuIds = new ArrayList<>();

    List<String> skuTempIds = new ArrayList<>();  /* use when creating new classify which leads to new skus with no id*/

    Integer orderClassify;

    boolean allowToRenameClassify = true;

    String blockRenamingClassifyMsg;

    boolean allowToRemoveClassify = true;

    String blockRemovingClassify;

    boolean allowToChangeClassifyImg = true;

    String blockChangingClassifyImgMsg;

}
