package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.PackageProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PackageProductEntityRepository extends JpaRepository<PackageProductEntity, Long> {
    List<PackageProductEntity> findByOrderPackage_IdAndLazbaoSkuIdIn(Long id, Collection<String> lazbaoSkuIds);

    @Modifying
    @Query("update PackageProductEntity p set p.negotiatedAmount = :amount where p.lazbaoSkuId in :skuIds")
    void resetAdjustedAmountBySkuIds(@Param("skuIds") List<String> skuIds, @Param("amount") BigDecimal amount);

    List<PackageProductEntity> findByOrderPackage_Id(Long id);
}

