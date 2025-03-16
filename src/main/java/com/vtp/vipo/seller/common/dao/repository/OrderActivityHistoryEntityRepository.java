package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderActivityHistoryEntity;
import com.vtp.vipo.seller.common.enumseller.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderActivityHistoryEntityRepository extends JpaRepository<OrderActivityHistoryEntity, Long> {
    List<OrderActivityHistoryEntity> findByOrderIdAndTypeOrderByCreatedAtDesc(Long orderId, ActivityType type);

    List<OrderActivityHistoryEntity> findByOrderIdAndType(Long orderId, ActivityType activityType);

    @Query(value = "SELECT * FROM order_activity_history o " +
            "WHERE o.orderId = :orderId " +
            "AND o.type = :type " +
            "AND JSON_EXTRACT(o.metadata, '$.visible') = true " +
            "ORDER BY o.createdAt DESC",
            nativeQuery = true)
    List<OrderActivityHistoryEntity> findVisibleMetadataRecords(
            @Param("orderId") Long orderId,
            @Param("type") ActivityType type);

    @Query(value = "SELECT * FROM order_activity_history o " +
            "WHERE o.id = :adjustmentHistoryId " +
            "AND JSON_EXTRACT(o.metadata, '$.visible') = true",
            nativeQuery = true)
    Optional<OrderActivityHistoryEntity> findVisibleMetadataRecord(@Param("adjustmentHistoryId") Long adjustmentHistoryId);
}
