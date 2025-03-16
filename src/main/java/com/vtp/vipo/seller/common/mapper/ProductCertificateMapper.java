package com.vtp.vipo.seller.common.mapper;

import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dto.response.ProductCertificateResponse;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductCertificateMapper {

    // Ánh xạ trường entity -> dto
    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "fileLink", source = "fileLink")
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "status", source = "status")
    ProductCertificateResponse toResponse(ProductCertificateEntity entity);

    // Ánh xạ ngược lại dto -> entity
    @InheritInverseConfiguration
    ProductCertificateEntity toEntity(ProductCertificateResponse response);

    // Map danh sách
    List<ProductCertificateResponse> toResponseList(List<ProductCertificateEntity> entities);

    List<ProductCertificateEntity> toEntityList(List<ProductCertificateResponse> responses);
}

