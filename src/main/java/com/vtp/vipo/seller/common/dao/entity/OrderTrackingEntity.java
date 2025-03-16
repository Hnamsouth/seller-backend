package com.vtp.vipo.seller.common.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Table(name = "order_tracking")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderTrackingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long packageId;

    String shipmentId;

    @Lob
    String sent;

    @Lob
    String content;

    String orderStatus;

    String statusOfPartner;

    Long time;

    String source;

    String uniqueKey;

    @Lob
    String data;

    @Column(unique = true)
    String msgId;
}
