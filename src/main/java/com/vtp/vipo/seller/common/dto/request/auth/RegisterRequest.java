package com.vtp.vipo.seller.common.dto.request.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RegisterRequest {

    private Long id;

    @NotBlank
    @Size(min = 1, max = 10, message = "Please enter a valid phone format")
    private String phone;

    @Size(min = 6, max = 25, message = "Password must be between 6 and 25 characters")
    private String password;

    private String email = "";

    private Boolean activated = true;

    private Integer areaId = 0;

    private Integer provinceId = 0;

    private Integer districtId = 0;

    private Integer wardId = 0;

    private String contactName = "";

    private Integer isApproved = 0;

    private Boolean stockManaged = false;

    private Integer pickType = 0;

    private Long joinTime = 0L;

    private Long lastLoginTime = 0L;

    private Integer status = 0;

    private Long staffId = 0L;

    private Integer isReject = 0;

    private Integer introduce = 0;

    private Long confirmTime = 0L;

    private Integer type = 0;

    private Integer isPostmall = 0;

    private Long firebaseTokenExpired = 0L;

    private Double commissionPercentForAffiliate = -1.00;

    private Boolean enableWholesale = false;

    private Integer selfShippingStatus = 0;

    private Long mygoTokenExpired = 0L;

    private Long groupByQTS = 0L;

    private Double rating = 0D;

    private Long approvedProductCount = 0L;

    private Long recoveryTime = 0L;

    private Integer isOCOP = 0;

    private Long ratingOCOP = 0L;

    private Long erpBranchId = 0L;

    private Integer enabledErp = 0;

    private Long erpPartnerId = 0L;

    private Integer enableBct = 0;

    private Boolean enableMarket = false;

    private Boolean allowSelfDelivery = false;

    private Boolean isPolicyTerms = false;

    private Long countryId;

}
