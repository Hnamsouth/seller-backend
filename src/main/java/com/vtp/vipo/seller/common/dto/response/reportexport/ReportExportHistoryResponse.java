package com.vtp.vipo.seller.common.dto.response.reportexport;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportExportHistoryResponse {

    String reportId;

    String reportType;

    String reportTypeDesc;

    String reportName;

    Long exportTime;

    String status;

    String downloadUrl;

    String errorMessage;

}
