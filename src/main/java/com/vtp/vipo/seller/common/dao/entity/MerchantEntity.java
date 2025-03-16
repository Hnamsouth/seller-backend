package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.enumseller.MerchantBusinessType;
import com.vtp.vipo.seller.common.utils.DateUtils;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Builder
@AllArgsConstructor
@Table(name = "merchant")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class MerchantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, columnDefinition = "tinyint default 0 comment 'Trạng thái // 0 chờ duyệt, 1 đã duyệt, 2 từ chối, 3 chờ duyệt sau khi sửa thông tin'")
    private Integer status;

    @Column(nullable = false, columnDefinition = "tinyint default 0 comment 'Trạng thái hoạt động // 0 đang hoạt động, 1 dừng hoạt động, 2 tạm khoá, 3 khoá vĩnh viễn, 4 deleted'")
    private Integer inactive;

    @Lob
    private String avatar;

    @Column(length = 20, nullable = true)
    private String contactPhone;

    @Column(length = 255, nullable = true)
    private String contactEmail;

    @Column(length = 255, nullable = true)
    private String contactName;

//    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 255, nullable = true)
    private String enterpriseCode;

    @Column(length = 255, nullable = true)
    private String taxCode;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private MerchantBusinessType businessType;

    @Lob
    private String businessLicenseImages;

    @Column(columnDefinition = "int default 0")
    private Integer countryId;

    @Column(columnDefinition = "int default 0")
    private Integer provinceId;

    @Column(columnDefinition = "int default 0")
    private Integer districtId;

    @Column(columnDefinition = "int default 0")
    private Integer wardId;

    @Lob
    private String address;

    @Lob
    private String fullAddress;

    @Column(columnDefinition = "tinyint default 0 comment 'Cho phép mở nhiều shop'")
    private Integer allowMultipleStores;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long createTime;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long updateTime;

    @Column
    private String accessToken;

    @Lob
    private String refreshToken;
    private Long expiredAt;
    @Column(name = "dateTaxCodeIssuance")
    private Date startTimeTaxcode;

    @Column(name = "placeTaxCodeIssuance")
    @Lob
    private String addressTaxcode;

    @Column(name = "agreeTerms")
    private Boolean isPolicyTerms;

    @Lob
    private String description;

    private Long vtpUserId;

    private Long merchantGroupId;

    @Transient
    private String sellerOpenId;

    @PrePersist
    protected void onCreate() {
        Long currentEpochSeconds = DateUtils.getCurrentTimeInSeconds();
        createTime = currentEpochSeconds;
        updateTime = currentEpochSeconds;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = DateUtils.convertMilTimeToSecond(System.currentTimeMillis());
    }
}
