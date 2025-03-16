package com.vtp.vipo.seller.common.dto.response.financial;

import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeRange {
    // Thời gian bắt đầu của kỳ hiện tại
    long currentStartSec;

    // Thời gian kết thúc của kỳ hiện tại
    long currentEndSec;

    // Thời gian bắt đầu của kỳ trước
    long previousStartSec;

    // Thời gian kết thúc của kỳ trước
    long previousEndSec;
}
