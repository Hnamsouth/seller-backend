package com.vtp.vipo.seller.common.dto.response;

import com.vtp.vipo.seller.common.enumseller.CertificateStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCertificateResponse {

    String id;

    Long productId;

    String name;

    String fileLink;

    String contentType;

    CertificateStatus status;
}
