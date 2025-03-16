package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ProductDailyAnalyticEntity;
import com.vtp.vipo.seller.common.dto.response.financial.TotalVisitsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ProductDailyAnalyticEntityRepository extends JpaRepository<ProductDailyAnalyticEntity, Long> {
    @Query("SELECT SUM(pda.viewCount) as totalVisits, MAX(pda.updatedAt) as updatedAt " +
            "FROM ProductDailyAnalyticEntity pda " +
            "WHERE pda.merchantId = :merchantId " +
            "AND pda.periodStart >= :currStart " +
            "AND pda.periodEnd <= :currEnd"
    )
    TotalVisitsProjection getTotalVisits(
            @Param("merchantId") Long merchantId,
            @Param("currStart") LocalDate currStart,
            @Param("currEnd") LocalDate currEnd
    );

    @Query("SELECT SUM(pda.viewCount) as totalVisits, MAX(pda.updatedAt) as updatedAt " +
            "FROM ProductDailyAnalyticEntity pda " +
            "WHERE pda.merchantId = :merchantId"
    )
    TotalVisitsProjection getTotalVisitsByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT p FROM ProductDailyAnalyticEntity p " +
            "WHERE p.productId IN :productIds " +
            "  AND p.merchantId IN :merchantIds " +
            "  AND p.periodStart IN :starts " +
            "  AND p.periodEnd IN :ends")
    List<ProductDailyAnalyticEntity> findAllByProductIdInAndMerchantIdInAndPeriodStartInAndPeriodEndIn(
            @Param("productIds") Set<Long> productIds,
            @Param("merchantIds") Set<Long> merchantIds,
            @Param("starts") Set<LocalDate> starts,
            @Param("ends") Set<LocalDate> ends
    );
}