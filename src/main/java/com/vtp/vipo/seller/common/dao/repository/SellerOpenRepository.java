package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.CategoryEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerOpenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerOpenRepository extends JpaRepository<SellerOpenEntity, Long> {
    Optional<SellerOpenEntity> findByMerchantId(Long merchantId);

    @Query(value = "select s.sellerOpenId from seller_open s where s.merchantId = :merchantId limit 1", nativeQuery = true)
    String getSellerOpenIdByMerchantId(@Param("merchantId") Long merchantId);
}
