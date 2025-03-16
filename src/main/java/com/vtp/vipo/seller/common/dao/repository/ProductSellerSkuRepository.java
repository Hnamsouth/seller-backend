package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ProductSellerSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSellerSkuRepository extends JpaRepository<ProductSellerSkuEntity, Long> {
    void deleteByProductId(@Param("productId") Long productId);
//    List<ProductSellerSkuEntity> findAllByProductId(@Param("productId") Long productId);

    List<ProductSellerSkuEntity> findAllByProductIdAndDeletedFalse(Long productId);

    List<ProductSellerSkuEntity> findByIdInAndProductId(@Param("id") List<Long> ids,
                                                         @Param("productId") Long productId);

    @Modifying
    @Query(value = "update product_seller_sku pss set pss.stock = 0 where pss.id in (select pp.lazbaoSkuId from package_product pp where pp.id in :ids)" , nativeQuery = true)
    void resetQuantityByIdIn(List<Long> ids);

    List<ProductSellerSkuEntity> findAllByIdIn(List<Long> ids);

}

