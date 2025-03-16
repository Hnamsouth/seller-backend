package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.CarrierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarrierEntityRepository extends JpaRepository<CarrierEntity, Long> {
    Optional<CarrierEntity> findByCarrierCode(String carrierCode);
}