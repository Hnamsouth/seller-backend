package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.ReportExportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

//todo: delete the table
//@Table(name = "report_exports")
@Table(name = "report_export")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExportEntity extends BaseEntity {

    private Long merchantId;

    private String reportType;

    private String reportSubType;

    private String reportFileName;

    private String exportReportRequest;

    private String filePath;

    private LocalDateTime finishTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportExportStatus status;

    private String errorMessage;

    private String storageType; //determine the storage type

    private String storageInfo; //determine the storage information to access the file
}
