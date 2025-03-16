package com.vtp.vipo.seller.common.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Table(name = "make_shipment_log")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MakeShipmentLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "packageId")
    Long packageId;

    @Column(name = "sent", columnDefinition = "LONGTEXT")
    String sent;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    String content;

    @Column(name = "time")
    Long time;

    @PrePersist
    public void prePersist() {
        time = System.currentTimeMillis() / 1000;
    }
}
