package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.RevenueReportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface RevenueReportEntityRepository extends JpaRepository<RevenueReportEntity, Long> {
    Optional<RevenueReportEntity> findByMerchantIdAndPeriodTypeAndPeriodStartAndPeriodEnd(Long merchantId, PeriodType periodType, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT r FROM RevenueReportEntity r WHERE r.merchantId = :merchantId " +
            "AND r.periodType = :periodType " +
            "AND r.periodStart >= :startDate " +
            "AND r.periodEnd <= :endDate")
    Optional<RevenueReportEntity> findReportByPeriod(Long merchantId, PeriodType periodType, LocalDate startDate, LocalDate endDate);

//    @Query(value = """
//            SELECT * FROM revenue_report r WHERE r.merchantId = :merchantId
//                    AND r.periodType = :periodType
//                    AND r.periodStart >= from_unixtime(:startDate)
//                    AND r.periodEnd <= from_unixtime(:endDate)
//        """, nativeQuery = true)
//    Optional<RevenueReportEntity> findByMerchantIdAndPeriodTypeAndPeriodStartAndPeriodEnd(Long merchantId, String periodType, long startDate, long endDate);

}