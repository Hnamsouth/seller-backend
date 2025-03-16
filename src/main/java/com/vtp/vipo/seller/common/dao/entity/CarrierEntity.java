package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Table(name = "carrier")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarrierEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    String carrierCode;

    @Column(nullable = false)
    String name;

    String contactInfo;
}
