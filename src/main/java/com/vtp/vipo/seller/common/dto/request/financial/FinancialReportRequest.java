package com.vtp.vipo.seller.common.dto.request.financial;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinancialReportRequest {
    String filterType; // day, week, month, quarter, year

    /*
    * Ngày: 2024-12-02
    * Tuần: 2024-W10
    * Tháng: 2024-12
    * Quý: 2024-Q4
    * Năm: 2024
    * */
    String filterValue;

    /* Phase6: Not use productIds (Next phase will use)*/
    List<Long> productIds = new ArrayList<>();
}
