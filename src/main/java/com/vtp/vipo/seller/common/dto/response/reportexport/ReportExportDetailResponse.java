package com.vtp.vipo.seller.common.dto.response.reportexport;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.ReportExportStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportExportDetailResponse {

    String id;

    String reportType;

    ReportExportStatus status;

    String errorMessage;

    Long createdAt;

    Long finishTime;

}
