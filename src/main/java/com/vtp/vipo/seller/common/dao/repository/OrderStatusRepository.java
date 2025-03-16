package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderStatusEntity;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderStatusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatusEntity, Long> {

    OrderStatusEntity findByStatusCodeAndRootCode(String statusCode, String rootCode);

    @Query( value = "SELECT a FROM OrderStatusEntity a WHERE a.statusCode IS NOT NULL AND a.rootCode = :rootCode")
    List<OrderStatusEntity> findAllByRootCode(String rootCode);

    @Query( value = "SELECT a FROM OrderStatusEntity a WHERE a.statusCode IS NOT NULL AND a.rootCode in :rootCodes")
    List<OrderStatusEntity> findAllStatusCodeByInsRootCode(List<String> rootCodes);

    @Query( value = "SELECT a FROM OrderStatusEntity a WHERE a.rootCode IS NULL")
    List<OrderStatusEntity> findALlRootStatus();

    @Query(value = """
            select o.*, osl.language, osl.name as nameInLanguage, osl.description as descriptionInLanguage
            from order_status o right join order_status_language osl on o.id =osl.orderStatusId
            """, nativeQuery = true)
    List<OrderStatusProjection> getAllOrderStatus();
}
