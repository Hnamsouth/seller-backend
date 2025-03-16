package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderPackageProjection;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderProjection;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestItemProjection;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialDashboardRawData;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialDataItemProjection;
import com.vtp.vipo.seller.common.dto.response.financial.TopProductSkuLineProjection;
import com.vtp.vipo.seller.common.dto.response.financial.TopProductProjection;
import com.vtp.vipo.seller.common.dto.response.order.OrderRefuseCancelCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderPackageRepository extends JpaRepository<OrderPackageEntity, Long> {

    Long countByOrderStatusInAndMerchantId(List<String> statuses, Long merchantId);

    @Query(value = "SELECT COUNT(op.id) FROM order_package op WHERE op.orderStatus IN (:statuses) AND DATE(FROM_UNIXTIME(op.createTime)) >= (:startDate)", nativeQuery = true)
    Long countOrder(List<String> statuses, String startDate);

    @Query(value = "SELECT SUM(op.totalPrice) FROM order_package op WHERE op.orderStatus = '501' AND DATE(FROM_UNIXTIME(op.createTime)) >= (:startDate)", nativeQuery = true)
    BigDecimal sumOrderSuccessful(String startDate);

    @Query("SELECT COUNT(o) > 0 FROM OrderPackageEntity o WHERE o.productId = :productId AND o.paymentTime IS NOT NULL")
    boolean existsByProductIdAndPaymentTimeNotNull(Long productId);

    @Query(value = """
            select     op.id,
                       op.orderCode,
                       op.orderStatus,
                       op.customerId,
                       c.displayName        as customerName,
                       c.avatar      as customerAvatar,
                       op.prePayment,
                       op.updateTime as updatedAt,
                       op.createTime as createdAt,
                       op.cancelTime,
                       op.cancelNote,
                       op.paymentTime,
                       op.deliverySuccessTime,
                       op.price + coalesce(op.totalDomesticShippingFee, op.totalShippingFee) as totalPayment,
                       (case when op.paymentTime  > 0 then op.prePayment else 0.0 end) as totalPaid,
                       op.productAmount,
                       op.returnTime,
                       (case when op.sellerOrderStatus = 'ORDER_SHIPMENT_CONNECTION_SUCCESS' then 'ORDER_PREPARED' else
                       op.sellerOrderStatus end) as sellerOrderStatus,
                       (case when op.sellerOrderStatus = 'ORDER_SHIPMENT_CONNECTION_SUCCESS' then 'Đã chuẩn bị hàng' else sos.name end) as sellerOrderStatusDesc,
                       os.shipmentCode,
                       os.createOrderMessage as shipmentMessage,
                       op.makeShipmentTime as shipmentTime,
                       op.isChangePrice as isChangePrice,
                       os.isPrinted,
                       os.createOrderStatus as shipmentStatus,
                       json_arrayagg(json_object(
                               'id', ppd.id,
                               'productId', ppd.productId,
                               'skuId', ppd.lazbaoSkuId,
                               'name', ppd.name,
                               'image', ppd.image,
                               'productAmount', ppd.productAmount,
                               'skuPrice', ppd.price * 1.00,
                               'quantity', ppd.quantity,
                               'merchantId', ppd.merchantId,
                               'specList', ppd.specMap,
                               'isOutOfStock', psk.stock = 0
                                     ))        as packageProductList
            from order_package op
                     left join order_shipment os on os.packageId = op.id
                     left join customer c on op.customerId = c.id
                     left join package_product ppd on ppd.packageId = op.id
                     left join product_seller_sku psk on ppd.lazbaoSkuId = psk.id
                     left join seller_order_status sos on sos.code = op.sellerOrderStatus
            where 
                (:merchantId IS NULL OR op.merchantId = :merchantId)
              and (:startDate IS NULL OR :endDate IS NULL OR (op.createTime > :startDate and op.createTime < :endDate))
              and (:tabCode IS NULL OR sos.parent_id = :tabCode)
               and op.orderStatus is not null and op.orderStatus != -1
              and (:orderCode IS NULL OR op.orderCode = :orderCode)
              and (:shipmentCode IS NULL OR op.shipmentId = :shipmentCode)
              and (:buyerName IS NULL OR LOWER(c.displayName) LIKE CONCAT('%', :buyerName, '%'))
              and (:productName IS NULL OR LOWER(ppd.name) LIKE CONCAT('%', :productName, '%'))
            group by op.id order by op.id desc
            """
            , countQuery = """
            select count(distinct op.id) from order_package op
                     left join customer c on op.customerId = c.id
                     left join package_product ppd on ppd.packageId = op.id
                     left join seller_order_status sos on sos.code = op.sellerOrderStatus
            where
                (:merchantId IS NULL OR op.merchantId = :merchantId)
              and (:startDate IS NULL OR :endDate IS NULL OR (op.createTime > :startDate and op.createTime < :endDate))
              and (:tabCode IS NULL OR sos.parent_id = :tabCode)
              and op.orderStatus is not null and op.orderStatus != -1
              and (:orderCode IS NULL OR op.orderCode = :orderCode)
              and (:shipmentCode IS NULL OR op.shipmentId = :shipmentCode)
              and (:buyerName IS NULL OR LOWER(c.displayName) LIKE CONCAT('%', :buyerName, '%'))
              and (:productName IS NULL OR LOWER(ppd.name) LIKE CONCAT('%', :productName, '%'))
    """
            , nativeQuery = true)
    Page<OrderProjection> findAllByMerchant(
            Integer tabCode,
            String orderCode,
            String buyerName,
            String productName,
            String shipmentCode,
            Long startDate,
            Long endDate,
            Long merchantId,
            Pageable pageable
    );

    @Query(value = """
            select     op.id,
                       op.orderCode,
                       op.orderStatus,
                       op.customerId,
                       c.displayName        as customerName,
                       c.avatar      as customerAvatar,
                       op.prePayment,
                       op.updateTime as updatedAt,
                       op.createTime as createdAt,
                       op.cancelTime,
                       op.cancelNote,
                       op.paymentTime,
                       op.deliverySuccessTime,
                       op.price + coalesce(op.totalDomesticShippingFee, op.totalShippingFee) as totalPayment,
                       (case when op.paymentTime  > 0 then op.prePayment else 0.0 end) as totalPaid,
                       op.productAmount,
                       op.returnTime,
                       (case when op.sellerOrderStatus = 'ORDER_SHIPMENT_CONNECTION_SUCCESS' then 'ORDER_PREPARED' else
                       op.sellerOrderStatus end) as sellerOrderStatus,
                       (case when op.sellerOrderStatus = 'ORDER_SHIPMENT_CONNECTION_SUCCESS' then 'Đã chuẩn bị hàng' else sos.name end) as sellerOrderStatusDesc,
                       os.shipmentCode,
                       os.createOrderMessage as shipmentMessage,
                       op.makeShipmentTime as shipmentTime,
                       op.isChangePrice as isChangePrice,
                       os.isPrinted,
                       os.createOrderStatus as shipmentStatus,
                       json_arrayagg(json_object(
                               'id', ppd.id,
                               'productId', ppd.productId,
                               'skuId', ppd.lazbaoSkuId,
                               'name', ppd.name,
                               'image', ppd.image,
                               'productAmount', ppd.productAmount,
                               'skuPrice', ppd.price * 1.00,
                               'quantity', ppd.quantity,
                               'merchantId', ppd.merchantId,
                               'specList', ppd.specMap,
                               'isOutOfStock', psk.stock = 0
                                     ))        as packageProductList
            from order_package op
                     left join order_shipment os on os.packageId = op.id
                     left join customer c on op.customerId = c.id
                     left join package_product ppd on ppd.packageId = op.id
                     left join product_seller_sku psk on ppd.lazbaoSkuId = psk.id
                     left join seller_order_status sos on sos.code = op.sellerOrderStatus
            where 
                (:merchantId IS NULL OR op.merchantId = :merchantId)
              and (:startDate IS NULL OR :endDate IS NULL OR (op.createTime > :startDate and op.createTime < :endDate))
              and (:tabCode IS NULL OR sos.parent_id = :tabCode)
              and op.orderStatus is not null and op.orderStatus != -1
              and op.sellerOrderStatus in :sellerOrderStatus
              and (:orderCode IS NULL OR op.orderCode = :orderCode)
              and (:shipmentCode IS NULL OR op.shipmentId = :shipmentCode)
              and (:buyerName IS NULL OR LOWER(c.displayName) LIKE CONCAT('%', :buyerName, '%'))
              and (:productName IS NULL OR LOWER(ppd.name) LIKE CONCAT('%', :productName, '%'))
            group by op.id order by op.id desc
            """
            , countQuery = """
            select count(distinct op.id) from order_package op
                     left join customer c on op.customerId = c.id
                     left join package_product ppd on ppd.packageId = op.id
                     left join seller_order_status sos on sos.code = op.sellerOrderStatus
            where
                (:merchantId IS NULL OR op.merchantId = :merchantId)
              and (:startDate IS NULL OR :endDate IS NULL OR (op.createTime > :startDate and op.createTime < :endDate))
              and (:tabCode IS NULL OR sos.parent_id = :tabCode)
              and op.orderStatus is not null and op.orderStatus != -1
              and op.sellerOrderStatus in :sellerOrderStatus
              and (:orderCode IS NULL OR op.orderCode = :orderCode)
              and (:shipmentCode IS NULL OR op.shipmentId = :shipmentCode)
              and (:buyerName IS NULL OR LOWER(c.displayName) LIKE CONCAT('%', :buyerName, '%'))
              and (:productName IS NULL OR LOWER(ppd.name) LIKE CONCAT('%', :productName, '%'))
    """
            , nativeQuery = true)
    Page<OrderProjection> findAllByMerchantV2(
            Integer tabCode,
            String orderCode,
            String buyerName,
            String productName,
            String shipmentCode,
            Long startDate,
            Long endDate,
            Long merchantId,
            List<String> sellerOrderStatus,
            Pageable pageable
    );

    @Query(value = " select op from OrderPackageEntity op where op.id in :ids")
    List<OrderPackageEntity> findAllByIdInAndOrderStatusIn(List<Long> ids);


    @Query("SELECT SUM(op.totalPrice) FROM OrderPackageEntity op WHERE op.orderStatus = '501' AND " +
            "(:startTime IS NULL OR op.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR op.createTime <= :endTime)")
    long sumOrderSuccessful(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Query("SELECT COUNT(op.id) FROM OrderPackageEntity op WHERE op.orderStatus IN (:statuses) AND " +
            "(:startTime IS NULL OR op.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR op.createTime <= :endTime)")
    long countOrder(@Param("statuses") List<String> statuses,
                    @Param("startTime") Long startTime,
                    @Param("endTime") Long endTime);
    List<OrderPackageEntity> findAllByIdInAndOrderStatusIn(List<Long> ids, List<String> orderStatus);
    @Query(value = """
            select op as orderPackage,
                (op.orderStatus in :orderStatus) as isBuyerCancel,
                (op.sellerOrderStatus = :sellerOrderStatus) isSellerCancel
                    from OrderPackageEntity op where op.id in :ids
            """)
    List<OrderRefuseCancelCheck> findAllByIdInAndOrderStatusIn(List<Long> ids, List<String> orderStatus, SellerOrderStatus sellerOrderStatus);

    @Modifying
    @Query("update OrderPackageEntity o set o.negotiatedAmount = :amount where o.id = :orderId")
    void updateAdjustedAmount(@Param("orderId") Long orderId, @Param("amount") BigDecimal amount);


    List<OrderPackageEntity> findByMerchantIdAndIdIn(Long merchantId, List<Long> ids);

    @Query(value = """
            select id, sellerOrderStatus
            from order_package
            where id in (:orderPackageIds) 
              and merchantId = :merchantId
            """,
            nativeQuery = true)
    List<OrderPackageProjection> getIdByMerchantIdAndIdIn(Long merchantId, List<Long> orderPackageIds);

    Optional<OrderPackageEntity> findByIdAndMerchantId(Long id, Long merchantId);

    List<OrderPackageEntity> findAllByMerchantIdAndIdIn(Long merchantId, List<Long> list);

    @Query(value = """
            select
                op.id as orderPackageId,
                op.orderCode as orderPackageCode,
                op.deliverySuccessTime as successDeliveryDate,
                (op.deliverySuccessTime + wc.withdrawAfterSecond) as withdrawalTime,
                os.shipmentCode as shippingCode,
                c.name as buyerName,
                op.quantity as quantity,
                op.prePayment as prepayment,
                (op.price * 30 / 100) + (IF(op.totalDomesticShippingFee = 0, op.totalShippingFee, op.totalDomesticShippingFee)) as codAmount,
                op.originPrice as orderAmount,
                (op.originPrice - op.price) as adjustmentPrice,
                op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit,
                (IF(sum(pp.sellerPlatformDiscountAmount) > 0,
                    json_array_append(json_arrayagg(json_object(
                            'fee_name', pfd.feeName,
                            'fee_value', pfd.feeValue,
                            'column_code', pfd.platformFeeId
                                                    )), '$', json_object(
                                              'fee_name', 'Chiết khấu sàn theo sản phẩm',
                                              'fee_value', sum(pp.sellerPlatformDiscountAmount),
                                              'column_code', 0)),
                    json_arrayagg(json_object(
                            'fee_name', pfd.feeName,
                            'fee_value', pfd.feeValue,
                            'column_code', pfd.platformFeeId
                                  )))) as fees
            from order_package op
                     join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
                     join customer c on op.customerId = c.id
                     join merchant m on op.merchantId = m.id
                     join `order` o on op.orderId = o.id
                     left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                     left join order_shipment os on os.packageId = op.id
                     left join platform_fee_detail pfd on pfd.packageId = op.id
            where op.merchantId = :merchantId
              and op.orderStatus = '501'
              and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
              and op.id not in (select distinct sub_wri.packageId
                                from withdrawal_request_item sub_wri
                                         left join withdrawal_request sub_wr on sub_wri.withdrawalRequestId = sub_wr.id
                                where sub_wr.merchantId = :merchantId
                                  and sub_wr.status not in ('REJECTED', 'CANCELED'))
              and(:shippingCode is null or os.shipmentCode = :shippingCode)
              and(:orderCode is null or op.orderCode = :orderCode)
              and(:buyerName is null or LOWER(c.name) LIKE CONCAT('%', LOWER(:buyerName), '%'))
              and(:startDate is null or (op.deliverySuccessTime + wc.withdrawAfterSecond) >= :startDate)
              and(:endDate is null or (op.deliverySuccessTime + wc.withdrawAfterSecond) <= :endDate)
            group by op.id, op.createTime order by op.createTime desc
        """,
    nativeQuery = true)
    Page<WithdrawalRequestItemProjection> getOrderPackageToWithdrawalByMerchantId(
            Long merchantId,
            String shippingCode,
            String orderCode,
            String buyerName,
            Long startDate,
            Long endDate,
            Pageable pageable);

    @Query(value = """
        select
            op.id as orderPackageId,
            op.orderCode as orderPackageCode,
            op.deliverySuccessTime as successDeliveryDate,
            (op.deliverySuccessTime + wc.withdrawAfterSecond) as withdrawalTime,
            op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit
        from order_package op
                 join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
                 join merchant m on op.merchantId = m.id
                 left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                 left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
          and op.id in (:packageIds)
          and op.orderStatus = '501'
          and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
          and op.id not in (select distinct sub_wri.packageId
              from withdrawal_request_item sub_wri
                       left join withdrawal_request sub_wr on sub_wri.withdrawalRequestId = sub_wr.id
              where sub_wri.packageId in (:packageIds)
                and sub_wr.status not in ('REJECTED', 'CANCELED'))
        group by op.id order by op.id
        """, nativeQuery = true)
    Collection<WithdrawalRequestItemProjection> getOrderPackageToWithdrawalByMerchantIdAndIdIn(Long merchantId, Collection<Long> packageIds);


    @Query(value = """
        select
            op.id as orderPackageId,
            op.orderCode as orderPackageCode,
            op.deliverySuccessTime as successDeliveryDate,
            (op.deliverySuccessTime + wc.withdrawAfterSecond) as withdrawalTime,
            op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit
        from order_package op
                 join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
                 join merchant m on op.merchantId = m.id
                 left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                 left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
          and op.id in (:packageIds)
          and op.orderStatus = '501'
          and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
        group by op.id order by op.id
        """, nativeQuery = true)
    Collection<WithdrawalRequestItemProjection> getOrderPackageByMerchantIdAndIdIn(Long merchantId, Collection<Long> packageIds);

    @Query(value = """
        select
            op.id as orderPackageId,
            op.orderCode as orderPackageCode,
            op.deliverySuccessTime as successDeliveryDate,
            (op.deliverySuccessTime + wc.withdrawAfterSecond) as withdrawalTime,
            os.shipmentCode as shippingCode,
            c.name as buyerName,
            op.quantity as quantity,
            op.prePayment as prepayment,
            (op.price * 30 / 100) + (IF(op.totalDomesticShippingFee = 0, op.totalShippingFee, op.totalDomesticShippingFee)) as codAmount,
            op.originPrice as orderAmount,
            (op.originPrice - op.price) as adjustmentPrice,
            op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit,
            (IF(sum(pp.sellerPlatformDiscountAmount) > 0,
                json_array_append(json_arrayagg(json_object(
                        'fee_name', pfd.feeName,
                        'fee_value', pfd.feeValue,
                        'column_code', pfd.platformFeeId
                                                )), '$', json_object(
                                          'fee_name', 'Chiết khấu sàn theo sản phẩm',
                                          'fee_value', sum(pp.sellerPlatformDiscountAmount),
                                          'column_code', 0)),
                json_arrayagg(json_object(
                        'fee_name', pfd.feeName,
                        'fee_value', pfd.feeValue,
                        'column_code', pfd.platformFeeId
                              )))) as fees
        from order_package op
                 join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
                 join customer c on op.customerId = c.id
                 join merchant m on op.merchantId = m.id
                 join `order` o on op.orderId = o.id
                 left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                 left join order_shipment os on os.packageId = op.id
                 left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
          and op.id in (:packageIds)
          and op.orderStatus = '501'
          and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
        group by op.id order by op.id
        """, nativeQuery = true)
    Collection<WithdrawalRequestItemProjection> getOrderPackageByMerchantIdAndIdInV2(Long merchantId, Collection<Long> packageIds);

    @Query(value = """
        select
            op.id as orderPackageId
        from order_package op
                 join package_product pp on pp.packageId = op.id
                 join merchant m on op.merchantId = m.id
                 left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                 left join platform_fee_detail pfd on pfd.packageId = op.id
        where op.merchantId = :merchantId
          and op.id in (:packageIds)
          and op.orderStatus = '501'
          and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
          and op.id not in (select distinct sub_wri.packageId
              from withdrawal_request_item sub_wri
                       left join withdrawal_request sub_wr on sub_wri.withdrawalRequestId = sub_wr.id
              where sub_wri.packageId in (:packageIds)
                and sub_wr.status not in ('REJECTED', 'CANCELED'))
        group by op.id order by op.id
        """, nativeQuery = true)
    Collection<Long> getOrderCanMakeWithdrawRequestByMerchantIdAndPackageIdIn(Long merchantId, Collection<Long> packageIds);

    @Query(value = """
            select
                op.id as orderPackageId,
                op.orderCode as orderPackageCode,
                op.deliverySuccessTime as successDeliveryDate,
                (op.deliverySuccessTime + wc.withdrawAfterSecond) as withdrawalTime,
                os.shipmentCode as shippingCode,
                c.name as buyerName,
                op.quantity as quantity,
                op.prePayment as prepayment,
                (op.price * 30 / 100) + (IF(op.totalDomesticShippingFee = 0, op.totalShippingFee, op.totalDomesticShippingFee)) as codAmount,
                op.originPrice as orderAmount,
                (op.originPrice - op.price) as adjustmentPrice,
                op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as estimatedProfit,
                (IF(sum(pp.sellerPlatformDiscountAmount) > 0,
                    json_array_append(json_arrayagg(json_object(
                            'fee_name', pfd.feeName,
                            'fee_value', pfd.feeValue,
                            'column_code', pfd.platformFeeId
                                                    )), '$', json_object(
                                              'fee_name', 'Chiết khấu sàn theo sản phẩm',
                                              'fee_value', sum(pp.sellerPlatformDiscountAmount),
                                              'column_code', 0)),
                    json_arrayagg(json_object(
                            'fee_name', pfd.feeName,
                            'fee_value', pfd.feeValue,
                            'column_code', pfd.platformFeeId
                                  )))) as fees
            from order_package op
                     join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
                     join customer c on op.customerId = c.id
                     join merchant m on op.merchantId = m.id
                     join `order` o on op.orderId = o.id
                     left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                     left join order_shipment os on os.packageId = op.id
                     left join platform_fee_detail pfd on pfd.packageId = op.id
            where 
              op.id in (:orderPackageIds)
              and op.merchantId = :merchantId
              and op.orderStatus = '501'
              and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
              and op.id not in (select distinct sub_wri.packageId
                                from withdrawal_request_item sub_wri
                                         left join withdrawal_request sub_wr on sub_wri.withdrawalRequestId = sub_wr.id
                                where sub_wr.merchantId = :merchantId
                                  and sub_wr.status not in ('REJECTED', 'CANCELED'))
            group by op.id order by op.id
        """,
            nativeQuery = true)
    Collection<WithdrawalRequestItemProjection> getOrderPackageReCreateByIdIn(Long merchantId, Collection<Long> orderPackageIds);

    @Query(value = """
        SELECT
           p.id AS productId,
           p.name AS productName,
           p.productCode,
           SUM(pp.quantity) AS quantitySold,
           SUM(pp.quantity * pp.price) AS revenueBeforeNegotiation,
           SUM(
               CASE
                   WHEN COALESCE(op.negotiatedAmount, 0) > 0
                        AND COALESCE(pp.negotiatedAmount, 0) = 0
                       THEN pp.quantity * (pp.price - (op.negotiatedAmount / pkg.totalQty))
                   WHEN COALESCE(op.negotiatedAmount, 0) = 0
                        AND COALESCE(pp.negotiatedAmount, 0) > 0
                       THEN pp.quantity * pp.negotiatedAmount
                   ELSE pp.quantity * pp.price
               END
           ) AS revenueAfterNegotiation,
           MAX(op.deliverySuccessTime) AS latestDeliverySuccessTime,
           MAX(p.createTime) AS productCreateTime
       FROM package_product pp
       JOIN (
           SELECT packageId, SUM(quantity) AS totalQty
           FROM package_product
           GROUP BY packageId
       ) pkg ON pkg.packageId = pp.packageId
       JOIN order_package op ON op.id = pp.packageId
       JOIN product p ON p.id = pp.merchantProductId
       WHERE op.sellerOrderStatus = 'ORDER_COMPLETED'
         AND op.merchantId = :merchantId
         AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
       GROUP BY p.id, p.productCode
       HAVING SUM(pp.quantity) > 0
       ORDER BY quantitySold DESC,
                revenueAfterNegotiation DESC,
                latestDeliverySuccessTime ASC,
                productCreateTime ASC
       LIMIT 5
    """, nativeQuery = true)
    List<TopProductProjection> findTopProducts(
            @Param("merchantId") Long merchantId,
            @Param("startTimeSeconds") long startTimeSeconds,
            @Param("endTimeSeconds") long endTimeSeconds
    );

    @Query(value = """
        SELECT
            COUNT(DISTINCT pp.merchantProductId) AS totalProducts
        FROM package_product pp
        JOIN order_package op ON op.id = pp.packageId
        WHERE op.sellerOrderStatus = 'ORDER_COMPLETED'
          AND op.merchantId = :merchantId
          AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
    """, nativeQuery = true)
    Long countUniqueProducts(@Param("merchantId") Long merchantId,
                             @Param("startTimeSeconds") long startTimeSeconds,
                             @Param("endTimeSeconds") long endTimeSeconds);

    @Query(value = """
        SELECT
            op.id                AS packageId,
            op.negotiatedAmount  AS opNegotiatedAmount,
            pp.merchantProductId AS productId,
            pp.lazbaoSkuId       AS skuId,
            MIN(pp.price)             AS price,
            MIN(pp.quantity)          AS quantity,
            MIN(pp.negotiatedAmount)  AS ppNegotiatedAmount,
            GROUP_CONCAT(
                DISTINCT CONCAT(sa.attributeName, ': ', sc.sellerName)
                SEPARATOR ', '
            ) AS label
    
        FROM order_package op
        JOIN package_product pp
             ON pp.packageId = op.id
        LEFT JOIN product_seller_sku pss
             ON pss.id = pp.lazbaoSkuId
        LEFT JOIN seller_classify sc
             ON FIND_IN_SET(sc.id, pss.sellerClassifyId) > 0
        LEFT JOIN seller_attribute sa
             ON sa.id = sc.sellerAttributeId
    
        WHERE
            op.sellerOrderStatus = 'ORDER_COMPLETED'
            AND op.merchantId = :merchantId
            AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
            AND pp.merchantProductId IN (:productIds)
    
        GROUP BY
            op.id,
            pp.merchantProductId,
            pp.lazbaoSkuId
    """, nativeQuery = true)
    List<TopProductSkuLineProjection> findAllSkuLinesByProductIds(
            @Param("merchantId") Long merchantId,
            @Param("productIds") List<Long> productIds,
            @Param("startTimeSeconds") long startTimeSeconds,
            @Param("endTimeSeconds") long endTimeSeconds
    );

    @Query(value = """
        SELECT COALESCE(SUM(op.price), 0) AS totalRevenue
        FROM order_package op
        WHERE op.sellerOrderStatus = 'ORDER_COMPLETED'
          AND op.merchantId = :merchantId
          AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
    """, nativeQuery = true)
    BigDecimal getTotalRevenue(
            @Param("merchantId") Long merchantId,
            @Param("startTimeSeconds") long startTimeSeconds,
            @Param("endTimeSeconds") long endTimeSeconds
    );

    @Query(value = """
    SELECT
        DATE(FROM_UNIXTIME(op.createTime)) AS periodName,
        COALESCE(SUM(op.price), 0) AS totalRevenue,
        (
            COALESCE(SUM(pp.totalPlatformDiscount), 0)
            + COALESCE(SUM(pfd.totalFeeValue), 0)
        ) AS totalCost,
        (
            COALESCE(SUM(op.price), 0)
            - (
                COALESCE(SUM(pp.totalPlatformDiscount), 0)
                + COALESCE(SUM(pfd.totalFeeValue), 0)
            )
        ) AS totalProfit
    FROM order_package op
    LEFT JOIN (
        SELECT
            packageId,
            SUM(sellerPlatformDiscountAmount) AS totalPlatformDiscount
        FROM package_product
        GROUP BY packageId
    ) AS pp ON pp.packageId = op.id
    LEFT JOIN (
        SELECT
            packageId,
            SUM(feeValue) AS totalFeeValue
        FROM platform_fee_detail
        GROUP BY packageId
    ) AS pfd ON pfd.packageId = op.id
    WHERE op.sellerOrderStatus = 'ORDER_COMPLETED'
      AND op.merchantId = :merchantId
      AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
    GROUP BY DATE(FROM_UNIXTIME(op.createTime))
    ORDER BY periodName;
    """, nativeQuery = true)
    List<FinancialDataItemProjection> getRevenueCostGroupByDay(
            @Param("merchantId") Long merchantId,
            @Param("startTimeSeconds") long startTimeSeconds,
            @Param("endTimeSeconds") long endTimeSeconds
    );

    @Query(value = """
    SELECT
        DATE_FORMAT(FROM_UNIXTIME(op.createTime), '%Y-%m') AS periodName,
        COALESCE(SUM(op.price), 0) AS totalRevenue,
        (
            COALESCE(SUM(pp.totalPlatformDiscount), 0)
            + COALESCE(SUM(pfd.totalFeeValue), 0)
        ) AS totalCost,
        (
            COALESCE(SUM(op.price), 0)
            - (
                COALESCE(SUM(pp.totalPlatformDiscount), 0)
                + COALESCE(SUM(pfd.totalFeeValue), 0)
            )
        ) AS totalProfit
    FROM order_package op
    LEFT JOIN (
        SELECT
            packageId,
            SUM(sellerPlatformDiscountAmount) AS totalPlatformDiscount
        FROM package_product
        GROUP BY packageId
    ) AS pp ON pp.packageId = op.id
    LEFT JOIN (
        SELECT
            packageId,
            SUM(feeValue) AS totalFeeValue
        FROM platform_fee_detail
        GROUP BY packageId
    ) AS pfd ON pfd.packageId = op.id
    WHERE op.sellerOrderStatus = 'ORDER_COMPLETED'
      AND op.merchantId = :merchantId
      AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
    GROUP BY DATE_FORMAT(FROM_UNIXTIME(op.createTime), '%Y-%m')
    ORDER BY periodName;
    """, nativeQuery = true)
    List<FinancialDataItemProjection> getRevenueCostGroupByMonth(
            @Param("merchantId") Long merchantId,
            @Param("startTimeSeconds") long startTimeSeconds,
            @Param("endTimeSeconds") long endTimeSeconds
    );

    @Query(value = """
    SELECT
         COUNT(DISTINCT CASE
             WHEN op.sellerOrderStatus IN (
                 'WAITING_FOR_SELLER_CONFIRMATION',
                 'WAITING_FOR_ORDER_PREPARATION',
                 'ORDER_PREPARED',
                 'ORDER_SHIPMENT_CONNECTION_SUCCESS',
                 'ORDER_DELIVERED_TO_SHIPPING',
                 'ORDER_IN_TRANSIT',
                 'ORDER_COMPLETED',
                 'SELLER_REJECTED_ORDER',
                 'ORDER_CANCELLED_BY_SELLER',
                 'ORDER_CANCELLED_BY_CUSTOMER',
                 'ORDER_CANCELLED_BY_VTP',
                 'ORDER_CANCELLED_BY_VIPO'
             ) THEN op.id
             ELSE NULL
         END) AS totalOrders,
         COUNT(DISTINCT CASE
             WHEN op.sellerOrderStatus IN (
                 'WAITING_FOR_ORDER_PREPARATION',
                 'ORDER_PREPARED',
                 'ORDER_SHIPMENT_CONNECTION_SUCCESS',
                 'ORDER_DELIVERED_TO_SHIPPING',
                 'ORDER_IN_TRANSIT',
                 'ORDER_COMPLETED',
                 'SELLER_REJECTED_ORDER',
                 'ORDER_CANCELLED_BY_SELLER',
                 'ORDER_CANCELLED_BY_CUSTOMER',
                 'ORDER_CANCELLED_BY_VTP',
                 'ORDER_CANCELLED_BY_VIPO'
             ) THEN op.customerId
             ELSE NULL
         END) AS totalBuyers,
         COUNT(DISTINCT CASE
             WHEN op.sellerOrderStatus IN (
                 'WAITING_FOR_ORDER_PREPARATION',
                 'ORDER_PREPARED',
                 'ORDER_SHIPMENT_CONNECTION_SUCCESS',
                 'ORDER_DELIVERED_TO_SHIPPING',
                 'ORDER_IN_TRANSIT',
                 'ORDER_COMPLETED',
                 'SELLER_REJECTED_ORDER',
                 'ORDER_CANCELLED_BY_SELLER',
                 'ORDER_CANCELLED_BY_CUSTOMER',
                 'ORDER_CANCELLED_BY_VTP',
                 'ORDER_CANCELLED_BY_VIPO'
             ) THEN op.id
             ELSE NULL
         END) AS convertedOrders,
         COUNT(DISTINCT CASE
             WHEN op.sellerOrderStatus = 'ORDER_COMPLETED'
             THEN op.id
             ELSE NULL
         END) AS deliveredOrders,
         COUNT(DISTINCT CASE
             WHEN op.sellerOrderStatus IN (
                 'SELLER_REJECTED_ORDER',
                 'ORDER_CANCELLED_BY_SELLER',
                 'ORDER_CANCELLED_BY_CUSTOMER',
                 'ORDER_CANCELLED_BY_VTP',
                 'ORDER_CANCELLED_BY_VIPO'
             ) THEN op.id
             ELSE NULL
         END) AS returnCancelOrders,
         COALESCE(
             SUM(
                 CASE WHEN op.sellerOrderStatus = 'ORDER_COMPLETED'
                      THEN op.price
                      ELSE 0
                 END
             ), 0
         ) AS revenue,
         COALESCE(
             SUM(
                 CASE WHEN op.sellerOrderStatus = 'ORDER_COMPLETED'
                      THEN pp.totalPlatformDiscount
                      ELSE 0
                 END
             ), 0
         ) AS platformFee,
         COALESCE(
             SUM(
                 CASE WHEN op.sellerOrderStatus = 'ORDER_COMPLETED'
                      THEN pfd.totalFeeValue
                      ELSE 0
                 END
             ), 0
         ) AS otherFee,
         COALESCE(
             SUM(
                 CASE WHEN op.sellerOrderStatus = 'ORDER_COMPLETED'
                      THEN (op.originPrice - op.price)
                      ELSE 0
                 END
             ), 0
         ) AS priceNegotiated
     FROM order_package op
     LEFT JOIN (
         SELECT
             packageId,
             SUM(sellerPlatformDiscountAmount) AS totalPlatformDiscount
         FROM package_product
         GROUP BY packageId
     ) pp ON pp.packageId = op.id
     LEFT JOIN (
         SELECT
             packageId,
             SUM(feeValue) AS totalFeeValue
         FROM platform_fee_detail
         GROUP BY packageId
     ) pfd ON pfd.packageId = op.id
     WHERE op.merchantId = :merchantId
       AND op.createTime BETWEEN :startTimeSeconds AND :endTimeSeconds
    """, nativeQuery = true)
    FinancialDashboardRawData getDashboardRawData(
            @Param("merchantId") Long merchantId,
            @Param("startTimeSeconds") long startTimeSeconds,
            @Param("endTimeSeconds") long endTimeSeconds
    );

    @Query(value = """
            select count(distinct op.orderCode)
            from order_package op
                     join merchant m on op.merchantId = m.id
                     left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                     left join platform_fee_detail pfd on pfd.packageId = op.id
            where op.id not in (select wri.packageId
                                from withdrawal_request_item wri
                                         join withdrawal_request wr on wr.id = wri.withdrawalRequestId
                                where wr.merchantId = :merchantId
                                  and wr.status in ('PENDING', 'PROCESSING', 'APPROVED', 'SUCCESS')
                                group by wri.packageId)
              and op.merchantId = :merchantId
              and op.orderStatus = '501'
              and ((op.deliverySuccessTime >= :startTime and op.deliverySuccessTime <= :endTime)
                or ((op.deliverySuccessTime + wc.withdrawAfterSecond) >= :startTime and
                    (op.deliverySuccessTime + wc.withdrawAfterSecond) <= :endTime))
            """, nativeQuery = true)
    Long countFinishedOrderPackage(long merchantId, long startTime, long endTime);
}
