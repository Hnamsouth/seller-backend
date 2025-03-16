package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderExtraServiceFeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderExtraServiceFeeEntityRepository extends JpaRepository<OrderExtraServiceFeeEntity, Long> {
    List<OrderExtraServiceFeeEntity> findByOrderId(Long orderId);
}
