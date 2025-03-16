package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.utils.DateUtils;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

/**
 * Author: hieuhm12
 * Date: 9/27/2024
 */
@Entity
@Table(name = "merchant_payment_card")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantPaymentCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Long merchantId;

    @Column(nullable = false, length = 255, columnDefinition = "varchar(255) not null comment 'Ngân hàng'")
    private String bankCode;

    @Column(nullable = false, length = 255, columnDefinition = "varchar(255) not null comment 'Số tài khoản'")
    private String accountNumber;

    @Column(nullable = false, length = 255, columnDefinition = "varchar(255) not null comment 'Chủ tài khoản'")
    private String accountOwner;

    @Lob
    @Column(nullable = false, columnDefinition = "text not null comment 'Chi nhánh'")
    private String branch;

    @Column(nullable = false, columnDefinition = "tinyint default 0 not null comment 'Mặc định'")
    private Integer isDefault;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long createTime;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
        updateTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
    }
}
