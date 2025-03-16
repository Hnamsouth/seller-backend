package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.v2.BaseEntityWithAutoIncrementIdAndEpochSecondsTime;
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
@Table(name = "withdrawal_config")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class WithdrawalConfigEntity extends BaseEntityWithAutoIncrementIdAndEpochSecondsTime {

    Long merchantGroupId;

//    Integer withdrawAfterDays;
//
//    Integer withdrawAfterHours;

    Long withdrawAfterSecond;

    Integer maxWithdrawalAttemptInAMonth;

    BigDecimal tax;

}
