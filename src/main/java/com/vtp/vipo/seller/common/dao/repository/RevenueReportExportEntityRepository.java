package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.RevenueReportExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.FinancialExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RevenueReportExportEntityRepository extends JpaRepository<RevenueReportExportEntity, Long> {

    Optional<RevenueReportExportEntity> findFirstByReportIdAndPeriodStartAndPeriodEndAndStatusInOrderByCreatedAtDescStatusAsc(
            Long reportId, LocalDate periodStart, LocalDate periodEnd, List<FinancialExportStatus> statuses
    );

}