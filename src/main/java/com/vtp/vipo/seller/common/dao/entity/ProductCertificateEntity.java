package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.enumseller.CertificateStatus;
import com.vtp.vipo.seller.common.enumseller.StorageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Table(name = "product_certificate")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCertificateEntity extends BaseEntity {

    Long productId;

    Long productTempId;

    String name;

    @Enumerated(EnumType.STRING)
    StorageType storageType;

    @Column(columnDefinition = "json")
    String storageInfo;

    Long fileSize;

    @Column(columnDefinition = "text")
    String fileLink;

    @Column(columnDefinition = "json")
    String representedImageLinks;

    String contentType;

    @Enumerated(EnumType.STRING)
    CertificateStatus status;

    @Enumerated(EnumType.STRING)
    CertificateStatus tempStatus;
}
