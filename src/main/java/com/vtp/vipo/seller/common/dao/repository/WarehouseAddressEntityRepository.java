package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WarehouseAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseAddressEntityRepository extends JpaRepository<WarehouseAddressEntity, Long> {
    Optional<WarehouseAddressEntity> findByIdAndMerchantIdAndDeletedFalse(Long id, Long merchantId);
}
