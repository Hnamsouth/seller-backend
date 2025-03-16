package com.vtp.vipo.seller.financialstatement.common.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.FinancialExportStatus;
import com.vtp.vipo.seller.financialstatement.common.enums.FinancialReportExportType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportExportInfoResponse {

    FinancialReportExportType reportType;

    FinancialExportStatus status;

    String reportUrl;

    String errorMessage;

}
