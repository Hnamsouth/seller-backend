package com.vtp.vipo.seller.common.dao.entity.enums.reportexport;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class S3StorageLocation {

    String bucket;

    String key;

}
