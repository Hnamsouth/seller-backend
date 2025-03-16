package com.vtp.vipo.seller.financialstatement.common.dto.request;

import com.vtp.vipo.seller.financialstatement.common.enums.FinancialReportFilterType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportExportInfoRequest {

    FinancialReportFilterType filterType;

    String filterValue;

    Long revenueReportId;

    List<Long> productIds = new ArrayList<>();

    String reportType;

}
