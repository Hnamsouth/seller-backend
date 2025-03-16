package com.vtp.vipo.seller.services.withdraw;

import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.WithdrawalRequestDetailResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ExcelService {
    ByteArrayOutputStream createExcelFile(WithdrawalRequestDetailResponse detail) throws IOException;
}
