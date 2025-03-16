package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.v2.BaseEntityWithSnowflakeIdAndEpochSecondsTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "withdrawal_request_item")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class WithdrawalRequestItemEntity extends BaseEntityWithSnowflakeIdAndEpochSecondsTime {

    Long withdrawalRequestId;

    Long packageId;

    BigDecimal withdrawAmount;

    BigDecimal withdrawableAmount;

    Boolean reCreated;

    Long withdrawableTime;

}
