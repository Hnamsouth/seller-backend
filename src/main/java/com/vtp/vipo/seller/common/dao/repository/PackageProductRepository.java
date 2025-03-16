package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.PackageProductEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.projections.MyData;
import com.vtp.vipo.seller.common.dao.entity.projections.SellerPackageProductProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface PackageProductRepository extends JpaRepository<PackageProductEntity, Long> {

    @Query(value = """
            select psk.id as skuId, psk.stock as skuStock, pp.*
            from package_product pp
            left join product_seller_sku psk on pp.lazbaoSkuId = psk.id
            where pp.packageId in (:packageIds)
               """, nativeQuery = true)
    List<SellerPackageProductProjection> getPackageProducByPackageIdIn(List<Long> packageIds);

    @Query(value = """
            select count(pp.id) 
            from package_product pp left join order_package op on pp.packageId = op.id
            where pp.merchantId = :merchantId
             and pp.packageId in (:packageIds)
            """,
            nativeQuery = true)
    Long countByMerchantIdAndPackageIdIn(Long merchantId, List<Long> packageIds);

    @Query(value = """
            select count(pp.id) 
            from PackageProductEntity pp 
                left join OrderPackageEntity op on pp.orderPackage = op
                left join OrderShipmentEntity os on op.id = os.packageId
                left join SellerOrderStatusEntity sos on op.sellerOrderStatus = sos.code
            where op.merchantId = :merchantId
                and op.createTime >= :startDate
                and op.createTime <= :endDate
                and (COALESCE(:sellerOrderStatuses, null) is null OR sos.code in :sellerOrderStatuses)
                and (COALESCE(:orderCode, null) is null OR lower(op.orderCode) like concat('%', :orderCode, '%'))
                and (COALESCE(:buyerName, null) is null OR lower(op.receiverName) like concat('%', :buyerName, '%'))
                and (COALESCE(:productName, null) is null OR lower(pp.name) like concat('%', :productName, '%'))
                and (
                        COALESCE(:shipmentCode, null) is null 
                        OR COALESCE(os.shipmentCode, null) is null 
                        OR lower(os.shipmentCode) like concat('%', :shipmentCode, '%')
                    )
            """)
    Long countByMerchantIdAndSellerOrderStatusIn(
            Long merchantId,
            List<String> sellerOrderStatuses,
            Long startDate,
            Long endDate,
            String orderCode,
            String buyerName,
            String productName,
            String shipmentCode
    );

    @Query(value = """
            select 
                op.id as packageId,
                pp.id as id,
                pp.lazbaoSkuId as skuId,
                psk.stock as skuStock,
                op.orderCode as orderCode,
                os.shipmentCode as shipmentCode,
                op.createTime as createTime,
                op.sellerOrderStatus as sellerOrderStatus,
                op.noteSeller as noteSeller,
                os.shipmentDate as shipmentDate,
                op.deliverySuccessTime as deliverySuccessTime,
                pp.name as skuName,
                psk.weight as skuWeight,
                c.name as categoryName,
                pp.price as skuPrice, 
                pp.negotiatedAmount as skuNegiotiatedAmount,
                op.negotiatedAmount as negotiatedAmount,
                op.refCode as refCode,
                pp.quantity as skuQuantity,
                op.totalPlatformFee as totalPlatformFee,
                op.prepayment as prepayment,
                c1.name as customerName,
                op.receiverName as receiverName,
                op.receiverPhone as receiverPhone,
                ew.name as receiverWard,
                ed.name as receiverDistrict,
                ep.name as receiverProvince,
                op.receiverAddress as receiverAddress, 
                op.totalPrice as totalPrice
            from package_product pp 
                left join order_package op on pp.packageId = op.id
                left join product_seller_sku psk on pp.lazbaoSkuId = psk.id
                left join order_shipment os on op.id = os.packageId
                left join product p on psk.productId = p.id
                left join category c on p.categoryId = c.id
                left join customer c1 on op.customerId = c1.id
                left join evtp_ward ew on op.receiverWardId = ew.id
                left join evtp_district ed on op.receiverDistrictId = ed.id
                left join evtp_province ep on op.receiverProvinceId = ep.id
            where op.id in (:selectedOrderIds)
            and op.merchantId = :merchantId
            order by op.createTime desc, op.id desc
            """, nativeQuery = true)
    List<MyData> getByMerchantIdAndPackageIdIn(Long merchantId, List<Long> selectedOrderIds, Pageable pageable);

    @Query(value = """
            SELECT
                op.id AS packageId,
                pp.id AS id,
                pp.lazbaoSkuId AS skuId,
                psk.stock AS skuStock,
                op.orderCode AS orderCode,
                os.shipmentCode AS shipmentCode,
                op.createTime AS createTime,
                op.sellerOrderStatus AS sellerOrderStatus,
                op.noteSeller AS noteSeller,
                os.shipmentDate AS shipmentDate,
                op.deliverySuccessTime AS deliverySuccessTime,
                pp.name AS skuName,
                psk.weight AS skuWeight,
                c.name AS categoryName,
                pp.price AS skuPrice,
                pp.negotiatedAmount AS skuNegotiatedAmount,
                op.negotiatedAmount AS negotiatedAmount,
                op.refCode AS refCode,
                pp.quantity AS skuQuantity,
                op.totalPlatformFee AS totalPlatformFee,
                op.prepayment AS prepayment,
                c1.name AS customerName,
                op.receiverName AS receiverName,
                op.receiverPhone AS receiverPhone,
                ew.name AS receiverWard,
                ed.name AS receiverDistrict,
                ep.name AS receiverProvince,
                op.receiverAddress AS receiverAddress,
                op.totalPrice AS totalPrice
            FROM package_product pp
                LEFT JOIN order_package op ON pp.packageId = op.id
                LEFT JOIN product_seller_sku psk ON pp.lazbaoSkuId = psk.id
                LEFT JOIN order_shipment os ON op.id = os.packageId
                LEFT JOIN product p ON psk.productId = p.id
                LEFT JOIN category c ON p.categoryId = c.id
                LEFT JOIN customer c1 ON op.customerId = c1.id
                LEFT JOIN evtp_ward ew ON op.receiverWardId = ew.id
                LEFT JOIN evtp_district ed ON op.receiverDistrictId = ed.id
                LEFT JOIN evtp_province ep ON op.receiverProvinceId = ep.id
            WHERE
                op.merchantId = :merchantId
                AND (:sellerOrderStatuses IS NULL OR op.sellerOrderStatus IN (:sellerOrderStatuses))
                AND (:startDate is null or op.createTime >= :startDate) 
                AND (:endDate is null or op.createTime <= :endDate)
                AND (:orderCode IS NULL OR LOWER(op.orderCode) LIKE CONCAT('%', :orderCode, '%'))
                AND (:buyerName IS NULL OR LOWER(op.receiverName) LIKE CONCAT('%', :buyerName, '%'))
                AND (:productName IS NULL OR LOWER(pp.name) LIKE CONCAT('%', :productName, '%'))
                AND (
                    :shipmentCode IS NULL
                    OR os.shipmentCode IS NULL
                    OR LOWER(os.shipmentCode) LIKE CONCAT('%', :shipmentCode, '%')
                )
            ORDER BY
                op.createTime DESC,
                op.id DESC
            """, nativeQuery = true)
    List<MyData> getByReportFilter(
            Long merchantId,
            List<SellerOrderStatus> sellerOrderStatuses,
            Long startDate,
            Long endDate,
            String orderCode,
            String buyerName,
            String productName,
            String shipmentCode,
            Pageable pageable
    );

    @Query(value = """
            select 
                op.id as packageId,
                pp.id as id,
                pp.lazbaoSkuId as skuId,
                psk.stock as skuStock,
                op.orderCode as orderCode,
                os.shipmentCode as shipmentCode,
                op.createTime as createTime,
                op.sellerOrderStatus as sellerOrderStatus,
                op.noteSeller as noteSeller,
                os.shipmentDate as shipmentDate,
                op.deliverySuccessTime as deliverySuccessTime,
                pp.name as skuName,
                pp.weight as skuWeight,
                c.name as categoryName,
                c.code as categoryCode,
                pp.price as skuPrice, 
                op.price as totalSkuPrice,
                op.originPrice as originTotalSkuPrice,
                pp.negotiatedAmount as skuNegotiatedAmount,
                op.negotiatedAmount as negotiatedAmount,
                op.refCode as refCode,
                pp.quantity as skuQuantity,
                op.totalPlatformFee as totalPlatformFee,
                op.prepayment as prepayment,
                c1.name as customerName,
                op.receiverName as receiverName,
                op.receiverPhone as receiverPhone,
                ew.name as receiverWard,
                ed.name as receiverDistrict,
                ep.name as receiverProvince,
                op.receiverAddress as receiverAddress, 
                op.totalPrice as totalPrice,
                o.paymentMethod as paymentMethod,
                pp.specMap as specMap,
                p.platformDiscountRate as platformDiscountRate,
                pp.sellerPlatformDiscountRate as sellerPlatformDiscountRate,
                pp.sellerPlatformDiscountAmount as sellerPlatformDiscountAmount,
                op.totalDomesticShippingFee as totalDomesticShippingFee,
                (
                           select JSON_OBJECTAGG(platformFeeId, feeValue)
                           from platform_fee_detail
                           where packageId = op.id
                       ) as platformFeeStr,
                pp.priceRanges as priceRanges,
                op.totalShippingFee as totalShippingFee,
                sos.name as sellerOrderStatusName,
                os.note as sellerNoteForShipment,
                os.expectedDeliveryTime as expectedDeliveryTime,
                op.isChangePrice as isChangePrice,
                op.paymentTime as paymentTime,
                op.deliveryStartTime as deliveryStartTime
            from package_product pp 
                left join order_package op on pp.packageId = op.id
                left join product_seller_sku psk on pp.lazbaoSkuId = psk.id
                left join order_shipment os on op.id = os.packageId
                left join product p on pp.merchantProductId = p.id
                left join category c on p.categoryId = c.id
                left join customer c1 on op.customerId = c1.id
                left join evtp_ward ew on op.receiverWardId = ew.id
                left join evtp_district ed on op.receiverDistrictId = ed.id
                left join evtp_province ep on op.receiverProvinceId = ep.id
                left join `order` o on op.orderId = o.id
                left join seller_order_status sos on op.sellerOrderStatus = sos.code
            where op.id in (:selectedOrderIds)
            and op.merchantId = :merchantId
            order by op.id desc
            """, nativeQuery = true)
    Stream<MyData> getByMerchantIdAndPackageIdInUsingStream(Long merchantId, List<Long> selectedOrderIds);

    @Query(value = """
            SELECT
                op.id AS packageId,
                pp.id AS id,
                pp.lazbaoSkuId AS skuId,
                psk.stock AS skuStock,
                op.orderCode AS orderCode,
                os.shipmentCode AS shipmentCode,
                op.createTime AS createTime,
                op.sellerOrderStatus AS sellerOrderStatus,
                op.noteSeller AS noteSeller,
                os.shipmentDate AS shipmentDate,
                op.deliverySuccessTime AS deliverySuccessTime,
                pp.name AS skuName,
                pp.weight AS skuWeight,
                c.name AS categoryName,
                c.code AS categoryCode,
                pp.price AS skuPrice,
                op.price as totalSkuPrice,
                op.originPrice as originTotalSkuPrice,
                pp.negotiatedAmount AS skuNegotiatedAmount,
                op.negotiatedAmount AS negotiatedAmount,
                op.refCode AS refCode,
                pp.quantity AS skuQuantity,
                op.totalPlatformFee AS totalPlatformFee,
                op.prepayment AS prepayment,
                c1.name AS customerName,
                op.receiverName AS receiverName,
                op.receiverPhone AS receiverPhone,
                ew.name AS receiverWard,
                ed.name AS receiverDistrict,
                ep.name AS receiverProvince,
                op.receiverAddress AS receiverAddress,
                op.totalPrice AS totalPrice,
                op.totalShippingFee as totalShippingFee,
                o.paymentMethod as paymentMethod,
                pp.specMap as specMap,
                p.platformDiscountRate as platformDiscountRate,
                pp.sellerPlatformDiscountRate as sellerPlatformDiscountRate,
                pp.sellerPlatformDiscountAmount as sellerPlatformDiscountAmount,
                op.totalDomesticShippingFee as totalDomesticShippingFee,
                (
                           select JSON_OBJECTAGG(platformFeeId, feeValue)
                           from platform_fee_detail
                           where packageId = op.id
                       ) as platformFeeStr,
                pp.priceRanges,
                op.totalShippingFee as expectedDomesticShippingFee,
                sos.name as sellerOrderStatusName,
                os.note as sellerNoteForShipment,
                os.expectedDeliveryTime as expectedDeliveryTime,
                op.isChangePrice as isChangePrice,
                op.paymentTime as paymentTime,
                op.deliveryStartTime as deliveryStartTime
            FROM package_product pp
                LEFT JOIN order_package op ON pp.packageId = op.id
                LEFT JOIN product_seller_sku psk ON pp.lazbaoSkuId = psk.id
                LEFT JOIN order_shipment os ON op.id = os.packageId
                LEFT JOIN product p ON pp.merchantProductId = p.id
                LEFT JOIN category c ON p.categoryId = c.id
                LEFT JOIN customer c1 ON op.customerId = c1.id
                LEFT JOIN evtp_ward ew ON op.receiverWardId = ew.id
                LEFT JOIN evtp_district ed ON op.receiverDistrictId = ed.id
                LEFT JOIN evtp_province ep ON op.receiverProvinceId = ep.id
                LEFT JOIN seller_order_status sos ON sos.code = op.sellerOrderStatus
                LEFT JOIN `order` o ON op.orderId = o.id
            WHERE
                op.merchantId = :merchantId
                AND (:startDate is null or op.createTime >= :startDate) 
                AND (:endDate is null or op.createTime <= :endDate)
                AND (:tabCode IS NULL OR sos.parent_id = :tabCode)
                AND (:orderCode IS NULL OR LOWER(op.orderCode) LIKE CONCAT('%', LOWER(:orderCode), '%'))
                AND (:buyerName IS NULL OR LOWER(op.receiverName) LIKE CONCAT('%', LOWER(:buyerName), '%'))
                AND (:productName IS NULL OR LOWER(pp.name) LIKE CONCAT('%', LOWER(:productName), '%'))
                AND (
                    :shipmentCode IS NULL
                    OR os.shipmentCode IS NULL
                    OR LOWER(os.shipmentCode) LIKE CONCAT('%', :shipmentCode, '%')
                )
            AND op.orderStatus IS NOT NULL AND op.orderStatus != -1
            ORDER BY
                op.id DESC
            """, nativeQuery = true)
    Stream<MyData> getByReportFilterUsingStream(
            Long merchantId,
            Integer tabCode,
            Long startDate,
            Long endDate,
            String orderCode,
            String buyerName,
            String productName,
            String shipmentCode
    );

    @Query("""
            select distinct pp.lazbaoSkuId
            from PackageProductEntity pp left join OrderPackageEntity op on pp.orderPackage = op
            where pp.productId = :id
            and op.orderStatus not in (:orderStatuses)
            """)
    List<Long> findAllSkuIdsInOrderPackageByProductId(Long id, List<String> orderStatuses);

    @Query(value = """
            SELECT
                op.id AS packageId,
                pp.id AS id,
                pp.lazbaoSkuId AS skuId,
                psk.stock AS skuStock,
                op.orderCode AS orderCode,
                os.shipmentCode AS shipmentCode,
                op.createTime AS createTime,
                op.sellerOrderStatus AS sellerOrderStatus,
                op.noteSeller AS noteSeller,
                os.shipmentDate AS shipmentDate,
                op.deliverySuccessTime AS deliverySuccessTime,
                pp.name AS skuName,
                pp.weight AS skuWeight,
                c.name AS categoryName,
                c.code AS categoryCode,
                pp.price AS skuPrice,
                op.price as totalSkuPrice,
                op.originPrice as originTotalSkuPrice,
                pp.negotiatedAmount AS skuNegotiatedAmount,
                op.negotiatedAmount AS negotiatedAmount,
                op.refCode AS refCode,
                pp.quantity AS skuQuantity,
                op.totalPlatformFee AS totalPlatformFee,
                op.prepayment AS prepayment,
                c1.name AS customerName,
                op.receiverName AS receiverName,
                op.receiverPhone AS receiverPhone,
                ew.name AS receiverWard,
                ed.name AS receiverDistrict,
                ep.name AS receiverProvince,
                op.receiverAddress AS receiverAddress,
                op.totalPrice AS totalPrice,
                op.totalShippingFee as totalShippingFee,
                o.paymentMethod as paymentMethod,
                pp.specMap as specMap,
                p.platformDiscountRate as platformDiscountRate,
                pp.sellerPlatformDiscountRate as sellerPlatformDiscountRate,
                pp.sellerPlatformDiscountAmount as sellerPlatformDiscountAmount,
                op.totalDomesticShippingFee as totalDomesticShippingFee,
                (
                           select JSON_OBJECTAGG(platformFeeId, feeValue)
                           from platform_fee_detail
                           where packageId = op.id
                       ) as platformFeeStr,
                pp.priceRanges,
                op.totalShippingFee as expectedDomesticShippingFee,
                sos.name as sellerOrderStatusName,
                os.note as sellerNoteForShipment,
                os.expectedDeliveryTime as expectedDeliveryTime,
                op.isChangePrice as isChangePrice,
                op.paymentTime as paymentTime,
                op.deliveryStartTime as deliveryStartTime
            FROM package_product pp
                LEFT JOIN order_package op ON pp.packageId = op.id
                LEFT JOIN withdrawal_request_item wri ON op.id = wri.packageId
                left join withdrawal_request_export wre on wri.withdrawalRequestId = wre.withdrawalRequestId
                LEFT JOIN product_seller_sku psk ON pp.lazbaoSkuId = psk.id
                LEFT JOIN order_shipment os ON op.id = os.packageId
                LEFT JOIN product p ON pp.merchantProductId = p.id
                LEFT JOIN category c ON p.categoryId = c.id
                LEFT JOIN customer c1 ON op.customerId = c1.id
                LEFT JOIN evtp_ward ew ON op.receiverWardId = ew.id
                LEFT JOIN evtp_district ed ON op.receiverDistrictId = ed.id
                LEFT JOIN evtp_province ep ON op.receiverProvinceId = ep.id
                LEFT JOIN seller_order_status sos ON sos.code = op.sellerOrderStatus
                LEFT JOIN `order` o ON op.orderId = o.id
            WHERE
                wre.id = :withdrawalRequestExportId
            ORDER BY
                op.id DESC
            """, nativeQuery = true)
    Stream<MyData> getByWithdrawalRequestExportId(Long withdrawalRequestExportId);
}
