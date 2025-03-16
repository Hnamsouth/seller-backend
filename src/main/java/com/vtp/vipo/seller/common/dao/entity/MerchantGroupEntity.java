package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.enumseller.MerchantBusinessType;
import com.vtp.vipo.seller.common.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Builder
@AllArgsConstructor
@Table(name = "merchant_group")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class MerchantGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
