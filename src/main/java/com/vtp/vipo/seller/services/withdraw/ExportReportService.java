package com.vtp.vipo.seller.services.withdraw;

import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestExportEntity;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.WithdrawalRequestDetailResponse;

public interface ExportReportService {
    void processExportReport(WithdrawalRequestDetailResponse detail, WithdrawalRequestExportEntity savedExportEntity);
}
