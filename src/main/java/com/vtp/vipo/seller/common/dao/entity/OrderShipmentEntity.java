package com.vtp.vipo.seller.common.dao.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.enumseller.ShippingConnectionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Table(name = "order_shipment")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderShipmentEntity extends BaseEntity {
    @Column(nullable = false)
    Long packageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrierId", nullable = false)
    CarrierEntity carrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouseAddressId", nullable = false)
    WarehouseAddressEntity warehouseAddress;

    String shipmentCode;

    LocalDateTime shipmentDate;

    String pickupAddress;

    @Enumerated(EnumType.STRING)
    ShippingConnectionStatus createOrderStatus;

    String status;

    String createOrderMessage;

    Boolean isPrinted;

    @Column(columnDefinition = "TEXT")
    String note;

    LocalDateTime expectedDeliveryTime;

}
