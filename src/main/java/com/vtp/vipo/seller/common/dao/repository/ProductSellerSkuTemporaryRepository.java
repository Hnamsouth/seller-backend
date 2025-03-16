package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ProductSellerSkuEntity;
import com.vtp.vipo.seller.common.dao.entity.ProductSellerSkuTemporaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSellerSkuTemporaryRepository extends JpaRepository<ProductSellerSkuTemporaryEntity, Long> {
    void deleteByProductTemporaryId(@Param("productTemporaryId") Long productTemporaryId);
    List<ProductSellerSkuTemporaryEntity> findAllByProductTemporaryId(@Param("productTemporaryId") Long productTemporaryId);
}

