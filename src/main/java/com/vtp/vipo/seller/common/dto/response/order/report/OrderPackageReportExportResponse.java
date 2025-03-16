package com.vtp.vipo.seller.common.dto.response.order.report;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderPackageReportExportResponse {

    String reportExportId;

}
