package com.vtp.vipo.seller.common.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StorageInfoDTO {
    String link;

    String bucketName;

    String key;
}
