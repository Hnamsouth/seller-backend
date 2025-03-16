package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.PlatformFeeDetailEntity;
import com.vtp.vipo.seller.common.dao.entity.projection.PlatformFeeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlatformFeeDetailRepository extends JpaRepository<PlatformFeeDetailEntity, Long> {

    List<PlatformFeeDetailEntity> findByPackageId(Long orderId);


    @Query(value = """
            select 
                distinct 
                pfd.platformFeeId as platformFeeId,
                pfd.feeName as feeName
            from order_package op
            left join platform_fee_detail pfd on op.id = pfd.packageId
            where op.id in (:selectedOrderIds)
            and op.merchantId = :merchantId
            """, nativeQuery = true)
    List<PlatformFeeProjection> getDistinctPlatformFeeByMerchantIdAndPackageIdIn(Long merchantId, List<Long> selectedOrderIds);

    @Query(value = """
            SELECT
                distinct 
                pfd.platformFeeId as platformFeeId,
                pfd.feeName as feeName
            FROM order_package op
            left join platform_fee_detail pfd on op.id = pfd.packageId
            LEFT JOIN seller_order_status sos ON sos.code = op.sellerOrderStatus
            LEFT JOIN package_product pp ON pp.packageId = op.id
            LEFT JOIN order_shipment os ON os.packageId = op.id
            WHERE
                op.merchantId = :merchantId
                AND (:tabCode IS NULL OR sos.parent_id = :tabCode)
                AND (:startDate is null or op.createTime >= :startDate) 
                AND (:endDate is null or op.createTime <= :endDate)
                AND (:orderCode IS NULL OR LOWER(op.orderCode) LIKE CONCAT('%', LOWER(:orderCode), '%'))
                AND (:buyerName IS NULL OR LOWER(op.receiverName) LIKE CONCAT('%', LOWER(:buyerName), '%'))
                AND (:productName IS NULL OR LOWER(pp.name) LIKE CONCAT('%', LOWER(:productName), '%'))
                AND (
                    :shipmentCode IS NULL
                    OR os.shipmentCode IS NULL
                    OR LOWER(os.shipmentCode) LIKE CONCAT('%', :shipmentCode, '%')
                )
            """, nativeQuery = true)
    List<PlatformFeeProjection> getDistinctPlatformFeeByReportFilter(
            Long merchantId,
            Integer tabCode,
            Long startDate,
            Long endDate,
            String orderCode,
            String buyerName,
            String productName,
            String shipmentCode
    );

    @Query(value = """
            SELECT
                distinct 
                pfd.platformFeeId as platformFeeId,
                pfd.feeName as feeName
            FROM order_package op
            left join platform_fee_detail pfd on op.id = pfd.packageId
            left join withdrawal_request_item wri on op.id = wri.packageId
            left join withdrawal_request_export wre on wri.withdrawalRequestId = wre.withdrawalRequestId
            WHERE
                wre.id = :withdrawalRequestReportId
            """, nativeQuery = true)
    List<PlatformFeeProjection> getDistinctPlatformFeeByWithdrawalRequestExportId(Long withdrawalRequestReportId);
}