package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.v2.BaseEntityWithSnowflakeIdAndEpochSecondsTime;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "withdrawal_request")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class WithdrawalRequestEntity extends BaseEntityWithSnowflakeIdAndEpochSecondsTime {

    Long merchantId;

    @Enumerated(EnumType.STRING)
    WithdrawRequestStatusEnum status;

    @Enumerated(EnumType.STRING)
    WithdrawalRequestType type;

    BigDecimal totalAmount;

    String cancelReason;

    BigDecimal tax;

    BigDecimal taxValue;

}
