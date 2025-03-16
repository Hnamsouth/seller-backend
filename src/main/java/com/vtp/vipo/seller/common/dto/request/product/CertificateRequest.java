package com.vtp.vipo.seller.common.dto.request.product;

import com.vtp.vipo.seller.common.enumseller.CertificateStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertificateRequest {
    String id;

    CertificateStatus status;
}
