package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.dao.entity.base.BaseEntityWithAutoIncrementId;
import com.vtp.vipo.seller.common.enumseller.FeeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Table(name = "platform_fee_detail")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformFeeDetailEntity extends BaseEntityWithAutoIncrementId {

    Long packageId;

    Long platformFeeId;

    @Enumerated(EnumType.STRING)
    FeeType feeType;

    @Column
    String feeDescription;

    BigDecimal feeValue;

    String feeName;

    @Column(columnDefinition = "JSON")
    String additionalData;

}
