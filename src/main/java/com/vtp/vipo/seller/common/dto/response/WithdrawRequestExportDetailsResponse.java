package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.base.v2.LocalDateTimeToLongAttributeConverter;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestReportTypeEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestExportEnum;
import jakarta.persistence.Convert;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WithdrawRequestExportDetailsResponse {
    String id;

    WithdrawRequestReportTypeEnum reportType;

    WithdrawalRequestExportEnum status;

    String errorMessage;

    Long createdAt;

    Long finishTime;
}
