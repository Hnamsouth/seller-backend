package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderTrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderTrackingEntityRepository extends JpaRepository<OrderTrackingEntity, Long> {
    List<OrderTrackingEntity> findByPackageId(Long orderId);

    @Query(value = "select * from order_tracking where packageId = :id order by time desc", nativeQuery = true)
    List<OrderTrackingEntity> findAllByPackageIdAndSourceOrOrderByTime(Long id);
}