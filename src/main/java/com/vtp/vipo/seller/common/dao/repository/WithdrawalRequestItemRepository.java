package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestItemEntity;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestItemRepository extends JpaRepository<WithdrawalRequestItemEntity, Long> {

//    TODO: calculate(estimatedRevenue) for estimatedProfit =
    @Query(value = """
        select 
            op.id as orderPackageId,
            op.orderCode as orderPackageCode,
            op.deliverySuccessTime as successDeliveryDate,
            wri.withdrawableTime as withdrawalTime,
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
        from withdrawal_request_item wri
                 join order_package op on wri.packageId = op.id
                 join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId from package_product ppd group by ppd.packageId) pp on pp.packageId = op.id
                 join customer c on op.customerId = c.id
                 join `order` o on op.orderId = o.id
                 join merchant m on op.merchantId = m.id
                 left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                 left join order_shipment os on os.packageId = op.id
                 left join platform_fee_detail pfd on pfd.packageId = op.id
            where
                wri.withdrawalRequestId = :requestId
                and wri.isDeleted = 0 
            group by op.id order by op.deliverySuccessTime desc
        """, nativeQuery = true
    )
    List<WithdrawalRequestItemProjection> getWithdrawalRequestItemByRequestId(Long requestId);


    @Query(value = """
        select wri.packageId from withdrawal_request_item wri
             join withdrawal_request wr on wri.withdrawalRequestId = wr.id
        where wri.packageId in (:packageIds)
        and wr.status not in ('REJECTED', 'CANCELED')
        group by wri.packageId
        """, nativeQuery = true)
    List<Long> getOrderPackageIdReCreatedByRequestIdAndPackageIdIn(List<Long> packageIds);

    @Query(value = """
        select wri.packageId from withdrawal_request_item wri where wri.withdrawalRequestId = :requestId
    """, nativeQuery = true)
    List<Long> getOrderPackageIdByRequestId(String requestId);

    @Modifying
    @Query(value = "update WithdrawalRequestItemEntity wri set wri.reCreated = true where wri.packageId in (:orderPackageIds)")
    void updateWithdrawItemReCreated(List<Long>orderPackageIds );

    @Query(value = """
            SELECT count(distinct wri.id)
            FROM withdrawal_request_item wri
                     LEFT JOIN withdrawal_request wr ON wri.withdrawalRequestId = wr.id
            WHERE wr.merchantId = :merchantId
              AND wr.status = 'SUCCESS'
              AND wr.withdrawSuccessTime >= :startTime
              AND wr.withdrawSuccessTime <= :endTime
                    """, nativeQuery = true)
    Long countWithdrawalRequestItem(Long merchantId, long startTime, long endTime);
}
