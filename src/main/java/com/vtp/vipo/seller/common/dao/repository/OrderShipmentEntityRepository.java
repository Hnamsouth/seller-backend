package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.OrderShipmentEntity;
import com.vtp.vipo.seller.common.enumseller.ShippingConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrderShipmentEntityRepository extends JpaRepository<OrderShipmentEntity, Long> {
    Optional<OrderShipmentEntity> findByPackageId(Long orderId);

    @Transactional
    @Modifying
    @Query("update OrderShipmentEntity o set o.isPrinted = :isPrinted where o.shipmentCode in :shipmentCodeList")
    void updateIsPrinted(List<String> shipmentCodeList, boolean isPrinted);

    List<OrderShipmentEntity> findByPackageIdInAndCreateOrderStatus(List<Long> orderIds, ShippingConnectionStatus createOrderStatus);

    Optional<OrderShipmentEntity> findByPackageIdAndCarrier_Id(Long packageId, Long id);
}
