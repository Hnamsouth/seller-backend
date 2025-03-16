package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Table(name = "package_log")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deliveryId")
    private long deliveryId;

    private String log;

    private String note;

    private String sent;

    private String data;

    @Column(name = "merchantId")
    private long merchantId;

    @Column(name = "customerId")
    private long customerId;

    @Column(name = "staffId")
    private long staffId;

    @Column(name = "isCancel")
    private int isCancel;

    @Column(name = "createTime")
    private long createTime;

    @Column(name = "packageId")
    private long packageId;

    public PackageLogEntity( String log, String note, String data, long merchantId, long customerId, int isCancel, long packageId) {
        this.log = log;
        this.note = note;
        this.data = data;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.isCancel = isCancel;
        this.packageId = packageId;
        this.createTime = DateUtils.getCurrentTimeInSeconds();
    }
}
