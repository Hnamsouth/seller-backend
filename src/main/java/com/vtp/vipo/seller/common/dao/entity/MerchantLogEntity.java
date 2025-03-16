package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.v2.LocalDateTimeToLongAttributeConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "merchant_log")
@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class MerchantLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long merchantId;

    @Builder.Default
    Long staffId = 0L;

    /**
     * The field below is equivalent to {@link com.vtp.vipo.seller.common.dao.entity.enums.MerchantLogActionEnum}
     *
     * However, we do not use enum because the data type in the database is not an enum which can cause unexpected behavior
     */
    String action;

    String note;

    String content;

    String data;

    @Convert(converter = LocalDateTimeToLongAttributeConverter.class)
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createTime;

    /* VIPO-3903: Upload e-contract: khi thông tin của nhà bán thay đổi thì sẽ lưu các thay đổi trên cột này */
    String dataChange;

}
