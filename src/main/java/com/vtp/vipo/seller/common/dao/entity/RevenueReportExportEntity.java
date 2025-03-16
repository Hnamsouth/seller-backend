package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.FinancialExportStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.FinancialReportType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "revenue_report_export")
@Entity
public class RevenueReportExportEntity extends BaseEntity {

    private Long reportId;

    @Enumerated(EnumType.STRING)
    private FinancialReportType reportType;

    private String reportName;

    private String filePath;

    private LocalDateTime exportTime;

    @Enumerated(EnumType.STRING)
    private FinancialExportStatus status;

    private String errorMessage;

    private String storageType; //determine the storage type

    private String storageInfo; //determine the storage information to access the file

    // Thời điểm bắt đầu báo cáo (DATE)
    private LocalDate periodStart;

    // Thời điểm kết thúc báo cáo (DATE)
    private LocalDate periodEnd;

}

