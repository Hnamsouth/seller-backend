package com.vtp.vipo.seller.financialstatement.common.dto.request;

import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportExportMsg {

    Long revenueReportExportId;

    FinancialReportRequest financialReportRequest;

    String version; // VIPO-3903: Upload E-Contract: check merchant to get the correct bank account

}
