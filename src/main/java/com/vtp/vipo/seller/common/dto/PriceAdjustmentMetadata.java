package com.vtp.vipo.seller.common.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceAdjustmentMetadata extends ActivityDetailsMetadata {
    String key;

    String beforeStatus;

    String afterStatus;

    Boolean visible;
}
