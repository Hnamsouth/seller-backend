package com.vtp.vipo.seller.common.dto.response.product.approve;

import com.vtp.vipo.seller.common.dao.entity.ProductEntity;
import com.vtp.vipo.seller.common.dao.entity.ProductSellerSkuEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerAttributeEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerClassifyEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ApproveProductResponse {

    ProductEntity product;

    List<ProductSellerSkuEntity> productSellerSkuEntities;

    List<SellerAttributeEntity> sellerAttributeEntities;

    List<SellerClassifyEntity> sellerClassifyEntities;

}
