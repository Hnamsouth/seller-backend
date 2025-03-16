package com.vtp.vipo.seller.common.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Author: hieuhm12
 * Date: 9/12/2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileAmazonS3Info {
    BigDecimal fileSizeMB;
    String contentType;
}
