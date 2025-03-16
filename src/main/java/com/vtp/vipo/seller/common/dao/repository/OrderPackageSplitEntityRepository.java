package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderPackageSplitEntityRepository extends JpaRepository<OrderPackageSplitEntity, Long> {
    Optional<OrderPackageSplitEntity> findByPackageId(Long orderId);
}