package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.ContractStatus;
import com.vtp.vipo.seller.common.dto.response.merchant.MerchantContractResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantResponse {

    private Long id;

    private String phone;

    private String email;

    private Boolean activated;

    private Long areaId;

    private Long provinceId;

    private Long districtId;

    private Long wardId;

    private String name;

    private String address;

    private String contactName;

    private Integer isApproved;

    private Boolean stockManaged;

    private Integer pickType;

    private Long joinTime;

    private Long lastLoginTime;

    private Integer status;

    private Long staffId;

    private Integer isReject;

    private Integer introduce;

    private Long confirmTime;

    private Integer type;

    private Integer isPostmall;

    private Long firebaseTokenExpired;

    private Double commissionPercentForAffiliate;

    private Boolean enableWholesale;

    private Integer selfShippingStatus;

    private Long mygoTokenExpired;

    private Long groupByQTS;

    private Double rating;

    private Long approvedProductCount;

    private Long recoveryTime;

    private Integer isOCOP;

    private Long ratingOCOP;

    private Long erpBranchId;

    private Integer enabledErp;

    private Long erpPartnerId;

    private Integer enableBct;

    private Boolean enableMarket;

    private Boolean allowSelfDelivery;

    private String paymentType;

    private String paymentName;

    private String paymentCard;

    private String paymentBank;

    private String paymentBranch;

    private String identityCard;

    private String taxCode;

    private String avatar;

    private String businessCode;

    private String descriptionShop;

    private Date startTimeTaxcode;

    private String addressTaxcode;

    private String licensePicture;

    private Boolean isPolicyTerms;
    private Integer inactive;

    private String countryName;

}
