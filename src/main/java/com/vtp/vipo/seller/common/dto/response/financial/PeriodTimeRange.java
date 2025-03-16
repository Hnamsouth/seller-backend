package com.vtp.vipo.seller.common.dto.response.financial;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PeriodTimeRange {
    // Thời gian bắt đầu của kỳ hiện tại
    LocalDateTime filterStartTime;

    // Thời gian kết thúc của kỳ hiện tại
    LocalDateTime filterEndTime;

    // Thời gian bắt đầu của kỳ trước
    LocalDateTime previousPeriodStartTime;

    // Thời gian kết thúc của kỳ trước
    LocalDateTime previousPeriodEndTime;
}