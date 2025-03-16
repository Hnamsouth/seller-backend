package com.vtp.vipo.seller.common.dto.response.product.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Author: hieuhm12
 * Date: 9/20/2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportByFileResultRes {
    String resultFileLink;
    String fileName;
    long totalCount;
    long successCount;
    long failureCount;
}
