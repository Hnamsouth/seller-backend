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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "withdrawal_config_history")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class WithdrawalConfigHistoryEntity extends BaseEntityWithAutoIncrementIdAndEpochSecondsTime {

    Long withdrawalConfigId;

    String before;

    String after;

}
