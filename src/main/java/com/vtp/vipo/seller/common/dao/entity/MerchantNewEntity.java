package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.enums.merchant.ContractStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.ContractStatusConverter;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.MerchantContractType;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.MerchantContractTypeConverter;
import com.vtp.vipo.seller.common.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Table(name = "merchant_new")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class MerchantNewEntity {

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

    @Lob
    private String businessLicenseImages;

    @Column(columnDefinition = "int default 0")
    private Long countryId;

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

    /* VIPO-3903: Upload e-contract: new fields */
    private Boolean enabledEditCountry;

    @Convert(converter = ContractStatusConverter.class)
    private ContractStatus contractStatus;

    @Convert(converter = MerchantContractTypeConverter.class)
    private MerchantContractType type;

    private String idCard;

    private LocalDate idCardIssueDate;

    private String idCardPlaceOfIssue;

    private String bankCode;

    private String bankBranch;

    private String bankNumber;

    private String bankOwner;

    private String businessName; //Tên doanh nghiệp

    private String businessNumber; //Mã số doanh nghiệp

    private String businessRepresent; //Người đại diện

    private String businessPosition; //Vị trí, chức vụ

}
