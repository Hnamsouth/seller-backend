package com.vtp.vipo.seller.common.dao.entity.base.v2;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntityWithAutoIncrementIdAndEpochSecondsTime extends BaseEntityWithEpochSecondsTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
