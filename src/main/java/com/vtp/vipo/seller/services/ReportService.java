package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dto.request.ReportRequest;
import com.vtp.vipo.seller.common.dto.response.ReportResponse;

public interface ReportService {

    ReportResponse reportFromMOIT(ReportRequest request);

}