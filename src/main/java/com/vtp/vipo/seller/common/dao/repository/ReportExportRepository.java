package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ReportExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.ReportExportStatus;
import com.vtp.vipo.seller.common.dto.response.ReportHistoryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ReportExportRepository extends JpaRepository<ReportExportEntity, Long> {

    @Query(value = """
        select  rp.id as reportId,
            rp.reportType as reportType,
            rp.reportSubType as reportTypeDesc,
            rp.reportFileName as reportName,
            UNIX_TIMESTAMP(rp.finishTime) as exportTime,
            rp.status as status,
            rp.filePath as downloadUrl,
            rp.errorMessage as errorMessage
        from report_export rp
        where
        rp.createdBy = :merchantId
        and (:reportName is null or lower(rp.reportFileName) like concat('%', lower(:reportName), '%'))
        and (:startDate is null or :endDate is null or
           (UNIX_TIMESTAMP(rp.finishTime) >= :startDate and UNIX_TIMESTAMP(rp.finishTime) <= :endDate))
        order by rp.finishTime desc, rp.createdAt desc
""",countQuery = """
        select count(rp.id)
        from report_export rp
        where
        rp.createdBy = :merchantId
        and (:reportName is null or lower(rp.reportFileName) like concat('%', lower(:reportName), '%'))
        and (:startDate is null or :endDate is null or
           (UNIX_TIMESTAMP(rp.finishTime) >= :startDate and UNIX_TIMESTAMP(rp.finishTime) <= :endDate))
""", nativeQuery = true)
    Page<ReportHistoryProjection> findAllByReportNameAndExportTime(String reportName, Long startDate, Long endDate,
            Long merchantId,
            Pageable pageable);

    @Query(value = """
            select re
            from ReportExportEntity re
            where re.merchantId = :merchantId
            and re.status not in :excludedStatuses
            and (:reportName is null or lower(re.reportFileName) like concat('%', lower(:reportName), '%'))
            and (:fromDate is null or re.createdAt >= :fromDate)
            and (:toDate is null or re.createdAt <= :toDate)
            and re.deleted = false
            order by re.createdAt desc
            """,
            countQuery = """
            select count(re.id)
            from ReportExportEntity re
            where re.merchantId = :merchantId
            and re.status not in :excludedStatuses
            and (:reportName is null or lower(re.reportFileName) like concat('%', lower(:reportName), '%'))
            and (:fromDate is null or re.createdAt >= :fromDate)
            and (:toDate is null or re.createdAt <= :toDate)
            and re.deleted = false
            """)
    Page<ReportExportEntity> findAllByReportExportByNameAndTimeRange(
            String reportName, LocalDateTime fromDate, LocalDateTime toDate, Long merchantId,
            List<ReportExportStatus> excludedStatuses, Pageable pageRequest
    );

    @Query(value = """
        select  rp.id as reportId,
                rp.reportType as reportType,
                 rp.reportSubType as reportTypeDesc,
                 rp.reportFileName as reportName,
                 UNIX_TIMESTAMP(rp.finishTime) as exportTime,
                 rp.status as status,
                 rp.filePath as downloadUrl,
                 rp.errorMessage as errorMessage
       from report_export rp
        where rp.id = :reportId
        and rp.merchantId = :merchantId
        and rp.isDeleted = 1
""", nativeQuery = true)
    Optional<ReportHistoryProjection> findReportExportById(Long reportId, Long merchantId);

    Optional<ReportExportEntity> findByIdAndMerchantIdAndDeleted(Long reportExportId, Long merchantId, boolean isDeleted);

    List<ReportExportEntity> findByDeletedAndCreatedAtBefore(boolean isDeleted, LocalDateTime cutOffTime);

    @Query("SELECT r FROM ReportExportEntity r WHERE r.deleted = false AND r.createdAt < :cutoffDate")
    Stream<ReportExportEntity> streamOldReports(LocalDateTime cutoffDate);
}
