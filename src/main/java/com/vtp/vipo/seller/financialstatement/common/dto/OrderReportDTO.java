package com.vtp.vipo.seller.financialstatement.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderReportDTO {

    Integer sequenceNum;

    String time;

    String orderPackageCode;

    String amount;

    String status;

}
