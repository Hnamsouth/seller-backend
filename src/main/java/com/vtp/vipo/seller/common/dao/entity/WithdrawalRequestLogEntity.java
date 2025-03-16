package com.vtp.vipo.seller.common.dao.entity;


import com.vtp.vipo.seller.common.dao.entity.base.v2.BaseEntityWithAutoIncrementIdAndEpochSecondsTime;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "withdrawal_request_log")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class WithdrawalRequestLogEntity extends BaseEntityWithAutoIncrementIdAndEpochSecondsTime {

    Long withdrawalRequestId;

    @Enumerated(EnumType.STRING)
    WithdrawRequestStatusEnum previousStatus;

    @Enumerated(EnumType.STRING)
    WithdrawRequestStatusEnum newStatus;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    Object beforeData;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    Object afterData;

    String remarks;
}
