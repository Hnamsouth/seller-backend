package com.vtp.vipo.seller.common.dao.entity.base.v2;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntityWithEpochSecondsTime implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column
    private String updatedBy;

    @Convert(converter = LocalDateTimeToLongAttributeConverter.class)
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Convert(converter = LocalDateTimeToLongAttributeConverter.class)
    @LastModifiedDate
    @Column
    private LocalDateTime updatedAt;

    @Column(name = "isDeleted")
    private boolean deleted = false;

    @Version
    @Column(name = "version")
    private Long version;
}
