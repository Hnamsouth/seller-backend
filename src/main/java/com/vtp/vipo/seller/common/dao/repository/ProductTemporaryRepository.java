package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ProductTemporaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductTemporaryRepository extends JpaRepository<ProductTemporaryEntity, Long> {
    Optional<ProductTemporaryEntity> findByProductId(@Param("productId") Long productId);
}

