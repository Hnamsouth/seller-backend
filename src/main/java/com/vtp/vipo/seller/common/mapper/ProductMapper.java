package com.vtp.vipo.seller.common.mapper;

import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dto.response.ProductBaseResponse;
import com.vtp.vipo.seller.common.dto.response.product.detail.SellerClassifyInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    void updateProductEntity(ProductTemporaryEntity tempProduct, @MappingTarget ProductEntity product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "id", target = "tempId")
    SellerAttributeEntity toSellerAttributeEntity(SellerAttributeTemporaryEntity sellerAttributeTemporaryEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "id", target = "tempId")
    @Mapping(source = "sellerAttributeTemporaryId", target = "sellerAttributeId")
    SellerClassifyEntity toSellerClassifyEntity(SellerClassifyTemporaryEntity sellerAttributeTemporaryEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(source = "id", target = "tempId")
    @Mapping(source = "sellerClassifyTemporaryId", target = "sellerClassifyId")
    ProductSellerSkuEntity toProductSellerSkuEntity(ProductSellerSkuTemporaryEntity sellerAttributeTemporaryEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "productId")
    ProductTemporaryEntity toProductTemporaryEntity(ProductEntity product);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "productId")
    void toProductTemporaryEntity(ProductEntity product, @MappingTarget ProductTemporaryEntity productTemp);

    @Mapping(source= "sellerName", target = "name")
    @Mapping(source= "sellerImage", target = "image")
    SellerClassifyInfo toSellerClassifyInfo(SellerClassifyEntity sellerClassifyEntity);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productName", source = "name")
    ProductBaseResponse toProductBaseResponse(ProductEntity productEntity);

    List<ProductBaseResponse> toProductBaseResponseList(List<ProductEntity> productEntities);
}
