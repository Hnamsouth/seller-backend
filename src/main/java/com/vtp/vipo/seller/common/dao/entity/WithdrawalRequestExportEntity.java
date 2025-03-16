package com.vtp.vipo.seller.common.dao.entity;


import com.vtp.vipo.seller.common.dao.entity.base.v2.BaseEntityWithSnowflakeIdAndEpochSecondsTime;
import com.vtp.vipo.seller.common.dao.entity.base.v2.LocalDateTimeToLongAttributeConverter;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestReportTypeEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestExportEnum;
import com.vtp.vipo.seller.common.enumseller.StorageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "withdrawal_request_export")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class WithdrawalRequestExportEntity extends BaseEntityWithSnowflakeIdAndEpochSecondsTime {

    Long withdrawalRequestId;

    @Enumerated(EnumType.STRING)
    WithdrawRequestReportTypeEnum reportType;

    String reportName;

    String filePath;

    @Convert(converter = LocalDateTimeToLongAttributeConverter.class)
    LocalDateTime exportTime;

    @Enumerated(EnumType.STRING)
    WithdrawalRequestExportEnum status;

    String errorMessage;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    private String storageInfo;

    @Convert(converter = LocalDateTimeToLongAttributeConverter.class)
    private LocalDateTime finishTime;
}
