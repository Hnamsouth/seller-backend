package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dto.response.OrderPackageResponse;
import com.vtp.vipo.seller.common.dto.response.OrderResponse;
import com.vtp.vipo.seller.common.dto.response.order.SellerOrderStatusProjection;
import com.vtp.vipo.seller.common.dto.response.order.SellerOrderStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query(value = "SELECT new com.vtp.vipo.seller.common.dto.response.OrderPackageResponse(a.orderCode, a.receiverName, a.receiverPhone, a.receiverAddress, b.name, b.specMap, b.skuImageUrl, b.quantity, a.totalPrice, (SELECT op.name FROM OrderStatusEntity op WHERE op.statusCode = os.rootCode), a.prePayment, a.createTime, a.updateTime, b.skuPrice, b.skuPriceRMB, a.paymentTime, b.productId, b.productSource, os.description, os.statusCode,ops.logisticsNo, a.cancelNote) " +
            "FROM OrderPackageEntity a " +
            "LEFT JOIN PackageProductEntity b ON a.id = b.orderPackage.id " +
            "LEFT JOIN OrderStatusEntity os ON a.orderStatus = os.statusCode " +
            "LEFT JOIN OrderPackageSplitEntity ops ON ops.packageId = a.id " +
            "WHERE (COALESCE(:childrenStatusCodes, null) IS NULL OR (a.orderStatus IN :childrenStatusCodes AND a.orderStatus IS NOT NULL)) " +
            "AND (:fromDate IS NULL OR a.createTime >= :fromDate) " +
            "AND (:toDate IS NULL OR a.createTime <= :toDate) " +
            "AND a.merchantId = :merchantId " +
            "AND (" +
            "    (:keySearch IS NULL OR :keySearch = '') " +
            "    OR (:keySearch = 'ORDER_CODE' AND a.orderCode = :searchQuery) " +
            "    OR (:keySearch = 'PRODUCT_NAME' AND COALESCE(b.name, '') LIKE CONCAT('%', :searchQuery, '%')) " +
            "    OR (:keySearch = 'BUYER_NAME' AND a.receiverName LIKE CONCAT('%', :searchQuery, '%')) " +
            "    OR (:keySearch = 'TRACKING_NUMBER' AND ops.logisticsNo = :searchQuery) " +
            "    OR (:keySearch = 'BUYER_PHONE_NUMBER' AND a.receiverPhone LIKE CONCAT('%', :searchQuery, '%')) " +
            ") " +
            "ORDER BY a.createTime DESC",
            countQuery = "SELECT COUNT(a.id) FROM OrderPackageEntity a " +
                    "LEFT JOIN PackageProductEntity b ON a.id = b.orderPackage.id " +
                    "LEFT JOIN OrderStatusEntity os ON a.orderStatus = os.statusCode " +
                    "LEFT JOIN OrderPackageSplitEntity ops ON ops.packageId = a.id " +
                    "WHERE (COALESCE(:childrenStatusCodes, null) IS NULL OR (a.orderStatus IN :childrenStatusCodes AND a.orderStatus IS NOT NULL)) " +
                    "AND (:fromDate IS NULL OR a.createTime >= :fromDate) " +
                    "AND (:toDate IS NULL OR a.createTime <= :toDate) " +
                    "AND a.merchantId = :merchantId " +
                    "AND (" +
                    "    (:keySearch IS NULL OR :keySearch = '') " +
                    "    OR (:keySearch = 'ORDER_CODE' AND a.orderCode = :searchQuery) " +
                    "    OR (:keySearch = 'PRODUCT_NAME' AND COALESCE(b.name, '') LIKE CONCAT('%', :searchQuery, '%')) " +
                    "    OR (:keySearch = 'BUYER_NAME' AND a.receiverName LIKE CONCAT('%', :searchQuery, '%')) " +
                    "    OR (:keySearch = 'TRACKING_NUMBER' AND ops.logisticsNo = :searchQuery) " +
                    "    OR (:keySearch = 'BUYER_PHONE_NUMBER' AND a.receiverPhone LIKE CONCAT('%', :searchQuery, '%')) " +
                    ") ")
    Page<OrderPackageResponse> getOrderPackageByFilters(@Param("childrenStatusCodes") List<String> childrenStatusCodes,
                                                @Param("fromDate") Long fromDate,
                                                @Param("toDate") Long toDate,
                                                @Param("merchantId") Long merchantId,
                                                @Param("searchQuery") String searchQuery,
                                                @Param("keySearch") String keySearch,
                                                Pageable pageable);

    @Query(value = "SELECT new com.vtp.vipo.seller.common.dto.response.OrderResponse(o.id, o.code, o.totalPaymentAmount, o.createTime, o.buyerPhone, o.buyerName, o.buyerAddress) " +
            "FROM OrderEntity o WHERE EXISTS (SELECT 1 FROM OrderPackageEntity op LEFT JOIN PackageProductEntity b ON op.id = b.orderPackage.id WHERE op.order.id = o.id AND op.merchantId = :merchantId " +
            "AND op.orderStatus in :childrenStatusCodes) " +
            "AND (:fromDate IS NULL OR o.createTime >= :fromDate) " +
            "AND (:toDate IS NULL OR o.createTime <= :toDate) " +
            "AND (:searchQuery IS NULL OR o.code LIKE %:searchQuery%) ORDER BY o.createTime DESC"
            , countQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE EXISTS (SELECT 1 FROM OrderPackageEntity op LEFT JOIN PackageProductEntity b ON op.id = b.orderPackage.id WHERE op.order.id = o.id AND  op.merchantId = :merchantId " +
            "AND op.orderStatus in :childrenStatusCodes)" +
            "AND (:fromDate IS NULL OR o.createTime >= :fromDate) " +
            "AND (:toDate IS NULL OR o.createTime <= :toDate) " +
            "AND (:searchQuery IS NULL OR o.code =:searchQuery)")
    Page<OrderResponse> getOrderByFilters(
            @Param("childrenStatusCodes") List<String> childrenStatusCodes,
            @Param("fromDate") Long fromDate,
            @Param("toDate") Long toDate,
            @Param("merchantId") Long merchantId,
            @Param("searchQuery") String searchQuery,
            Pageable pageable);


    @Query(value = "SELECT new com.vtp.vipo.seller.common.dto.response.OrderResponse(o.id, o.code, o.totalPaymentAmount, o.createTime, o.buyerPhone, o.buyerName, o.buyerAddress) " +
            "FROM OrderEntity o WHERE EXISTS (SELECT 1 FROM OrderPackageEntity op WHERE op.order.id = o.id AND op.merchantId = :merchantId " +
            "AND o.code = :orderCode)")
    OrderResponse findOrdersByOrderCodeAndMerchant(
            @Param("orderCode") String orderCode,
            @Param("merchantId") Long merchantId);

    @Query(value = "SELECT new com.vtp.vipo.seller.common.dto.response.OrderPackageResponse(a.orderCode, a.receiverName, a.receiverPhone, a.receiverAddress, b.name, b.specMap, b.skuImageUrl, b.quantity, a.totalPrice, (SELECT op.name FROM OrderStatusEntity op WHERE op.statusCode = os.rootCode), a.prePayment, a.createTime, a.updateTime, b.skuPrice, b.skuPriceRMB, a.paymentTime, b.productId, b.productSource, os.description, os.statusCode,ops.logisticsNo, a.cancelNote) " +
            "FROM OrderPackageEntity a " +
            "LEFT JOIN PackageProductEntity b ON a.id = b.orderPackage.id " +
            "LEFT JOIN OrderStatusEntity os ON a.orderStatus = os.statusCode " +
            "LEFT JOIN OrderPackageSplitEntity ops ON ops.packageId = a.id " +
            "WHERE (COALESCE(:childrenStatusCodes, null) IS NULL OR (a.orderStatus IN :childrenStatusCodes AND a.orderStatus IS NOT NULL)) " +
            "AND a.order.id = :code " +
            "AND a.merchantId = :merchantId ")
    List<OrderPackageResponse> getListOrderPackage(
            @Param("childrenStatusCodes") List<String> childrenStatusCodes,
            @Param("code") Long code,
            @Param("merchantId") Long merchantId);


    @Query(value = "SELECT new com.vtp.vipo.seller.common.dto.response.OrderPackageResponse(a.orderCode, a.receiverName, a.receiverPhone, a.receiverAddress, b.name, b.specMap, b.skuImageUrl, b.quantity, a.totalPrice, (SELECT op.name FROM OrderStatusEntity op WHERE op.statusCode = os.rootCode), a.prePayment, a.createTime, a.updateTime, b.skuPrice, b.skuPriceRMB, a.paymentTime, b.productId, b.productSource, os.description, os.statusCode,ops.logisticsNo, a.cancelNote) " +
            "FROM OrderPackageEntity a " +
            "LEFT JOIN PackageProductEntity b ON a.id = b.orderPackage.id " +
            "LEFT JOIN OrderStatusEntity os ON a.orderStatus = os.statusCode " +
            "LEFT JOIN OrderPackageSplitEntity ops ON ops.packageId = a.id " +
            "AND a.orderCode = :code " +
            "AND a.merchantId = :merchantId ")
    List<OrderPackageResponse> findOrderPackageByCodeAndMerchantId(
            @Param("merchantId") Long merchantId,
            @Param("code") Long code
            );

    @Query(value = "SELECT COUNT(o) FROM OrderEntity o WHERE EXISTS (SELECT 1 FROM OrderPackageEntity op WHERE op.order.id = o.id AND op.merchantId = :merchantId AND op.orderStatus in :chillStatusCodes) ")
    Long countByOrderStatus(
            @Param("chillStatusCodes") List<String> chillStatusCodes,
            @Param("merchantId") Long merchantId);

    @Query(value = "SELECT SUM(o.totalPrice) FROM OrderPackageEntity o where o.merchantId = :merchantId AND o.orderStatus in :chillStatusCodes")
    BigDecimal sumOrderByMerchant(
            @Param("chillStatusCodes") List<String> chillStatusCodes,
            @Param("merchantId") Long merchantId);

    @Query(value = """
        select sos.parent_id statusGroup,
               count(op.id) toltalOrder
        from seller_order_status sos
        left join order_package op on op.sellerOrderStatus = sos.code and op.merchantId = :merchantId 
        where sos.parent_id is not null
        and(op.id is null or (op.orderStatus is not null and op.orderStatus !=-1))
        group by sos.parent_id
        order by sos.parent_id
""", nativeQuery = true)
    List<SellerOrderStatusProjection> countSellerOrderbyMerchantId(Long merchantId);

    @Query(value = """
         select 
            sos.parent_id statusGroup,
             count(op.id) toltalOrder
         from seller_order_status sos
             join order_package op on op.sellerOrderStatus = sos.code and op.merchantId = :merchantId
         where 
             op.orderStatus > -1
            and sos.parent_id is not null
         group by sos.parent_id
""", nativeQuery = true)
    List<SellerOrderStatusProjection> countSellerOrderbyMerchantIdV2(Long merchantId);

    @Query(value = """
        select new com.vtp.vipo.seller.common.dto.response.order.SellerOrderStatusResponse(sos.code, sos.name, count(op.id))
             from SellerOrderStatusEntity sos
                  left join OrderPackageEntity op on sos.code = op.sellerOrderStatus and op.merchantId = :merchantId
             where
                sos.parentId = :groupIdx
                and (op.id is null or (op.orderStatus is not null and op.orderStatus != '-1'))
             group by sos.code, sos.name order by sos.code desc
""")
    List<SellerOrderStatusResponse> countWaitingShipmentSellerOrderbyMerchantId(Long merchantId, Integer groupIdx);

}
