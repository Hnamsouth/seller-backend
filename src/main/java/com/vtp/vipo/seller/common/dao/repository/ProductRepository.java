package com.vtp.vipo.seller.common.dao.repository;


import com.vtp.vipo.seller.common.dao.entity.ProductEntity;
import com.vtp.vipo.seller.common.dto.response.ProductBaseResponse;
import com.vtp.vipo.seller.common.dto.response.product.search.ProductSearchRes;
import com.vtp.vipo.seller.common.enumseller.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    ProductEntity findByIdAndMerchantIdAndIsDeleted(Long id, Long merchantId, int isDeleted);

    @Query(value = "SELECT p.productSkuInfoList FROM product p WHERE p.isDeleted =0 AND (DATE(FROM_UNIXTIME(p.createTime)) = (:dateNow) OR :dateNow IS NULL)", nativeQuery = true)
    List<String> findProductSkuByCreateTime(String dateNow);

    //todo: remove the use of the constructor
    @Query("SELECT new com.vtp.vipo.seller.common.dto.response.product.search.ProductSearchRes( " +
            "    p.id, p.productCode, p.name, p.image, " +
//            "    COUNT(DISTINCT sa.id), " +
            "    COUNT(DISTINCT sa.sellerAttributeId), " +
            "    COUNT(DISTINCT pss.id), " +
            "    p.productPriceType, p.price ," +
//            "    CASE WHEN p.price IS NOT NULL THEN p.price ELSE -1L END, " +
            "    CONCAT(COALESCE(cl2.name, c2.name, cl.name, c.name), " +
            "    CASE WHEN c2.name IS NOT NULL THEN CONCAT(' >> ', COALESCE(cl.name, c.name)) ELSE '' END), " +
            "    COALESCE(p.buyCount, 0), " +  // Dùng COALESCE để xử lý null cho buyCount
            "    (SELECT COALESCE(SUM(pss2.stock), 0) FROM ProductSellerSkuEntity pss2 WHERE pss2.productId = p.id),  " +  // Xử lý null cho tổng stock
            "    COALESCE(p.numProdWaitingSold, 0), " +  // Xử lý null cho số sản phẩm chờ bán
            "    p.status , p.platformDiscountRate, p.createTime, p.updateTime" +
            "  ) " +
            "FROM ProductEntity p " +
            "LEFT JOIN SellerClassifyEntity sa ON sa.productId = p.id " +
            "LEFT JOIN ProductSellerSkuEntity pss ON pss.productId = p.id " +
            "LEFT JOIN CategoryEntity c ON p.categoryId = c.id " +
            "LEFT JOIN CategoryLanguageEntity cl ON cl.categoryId = c.id AND cl.language = :language " +
            "LEFT JOIN CategoryEntity c2 ON c.parentId = c2.id " +
            "LEFT JOIN CategoryLanguageEntity cl2 ON cl2.categoryId = c2.id AND cl2.language = :language " +
            "WHERE p.isDeleted = 0 " +
//            "  AND  pss.activeStatus = true " +
            "  AND (:keyword IS NULL OR p.name LIKE :keyword " +
            "  OR  p.productCode LIKE :keyword OR CAST(p.id AS string) LIKE :keyword " +
            "  OR pss.code LIKE :keyword) " +
            "  AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
            "  AND p.merchantId = :merchantId " +
            "  AND (:status IS NULL OR p.status = :status) " +
            "  AND (:fromDate IS NULL OR p.createTime >= :fromDate) " +
            "  AND (:toDate IS NULL OR p.createTime <= :toDate) " +
            "GROUP BY p.id, p.productCode, p.name, p.image, p.productPriceType, p.price, " +
            "         c.name, p.buyCount, p.numProdWaitingSold, p.status, p.updateTime ")
    Page<ProductSearchRes> searchProducts(@Param("keyword") String keyword,
                                          @Param("categoryId") Integer categoryId,
                                          @Param("status") ProductStatus status,
                                          @Param("fromDate") Long fromDate,
                                          @Param("toDate") Long toDate,
                                          @Param("merchantId") Long merchantId,
                                          @Param("language") String language,
                                          Pageable pageable);

    @Query("SELECT p.productCodeCustomer FROM ProductEntity p WHERE p.productCodeCustomer IN :codes" +
            " AND p.merchantId = :merchantId AND (:productId is null or p.id != :productId) ")
    List<String> findProductCustomerCodeByCodes(@Param("codes") List<String> codes,
                                                @Param("merchantId") Long merchantId,
                                                @Param("productId") Long productId);

    @Query("SELECT count(p) FROM ProductEntity p WHERE p.isDeleted = 0 AND p.status = 4 AND " +
            "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR p.createTime <= :endTime)")
    long countNewProducts(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<ProductEntity> findAllByIdIn(Collection<Long> id);

    @Query(
            "SELECT p " +
                    "FROM ProductEntity p " +
                    "WHERE p.merchantId = :merchantId " +
                    "  AND ( :keyword IS NULL OR :keyword = '' " +
                    "        OR UPPER(p.name) LIKE CONCAT('%', UPPER(:keyword), '%') ) " +
                    "  AND p.isDeleted = 0 " +
                    "ORDER BY p.createTime DESC"
    )
    Page<ProductEntity> filterProduct(@Param("merchantId") Long merchantId,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);

    List<ProductEntity> findByIdInAndMerchantIdAndIsDeleted(List<Long> ids, Long merchantId, Integer isDeleted);

    @Query("SELECT p.id FROM ProductEntity p WHERE p.merchantId = :merchantId AND p.isDeleted = 0")
    List<Long> findProductIdsByMerchantId(@Param("merchantId") Long merchantId);
}