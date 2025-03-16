package com.vtp.vipo.seller.services.authen.impl.strategies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response object for the result of a user's information update using SSO.
 *
 * <p>This object is deserialized from the VTP API's response and provides details
 * about the status, message, and user information.</p>
 */
@Data
public class SsoUpdateUserResponse {

    private Integer status;

    private Boolean error;

    private String message;

    private DataResponse data;

    @JsonProperty("messageKey")
    private String messageKey;

    @Data
    public static class DataResponse {

        private String refreshToken;

        private String customerType;

        @JsonProperty("Partner")
        private Integer partner;

        @JsonProperty("UserName")
        private String userName;

        @JsonProperty("UserId")
        private Long userId;

        @JsonProperty("Role")
        private Long role;

        @JsonProperty("FromSource")
        private String fromSource;

        @JsonProperty("TokenKey")
        private String tokenKey;

        @JsonProperty("Phone")
        private String phone;

        @JsonProperty("Email")
        private String email;

        @JsonProperty("CreatedDate")
        private String createdDate;

        @JsonProperty("CheckClientNew")
        private String checkClientNew;

        @JsonProperty("UserInfoResponse")
        private UserInfoResponse userInfoResponse;
    }

    @Data
    public static class UserInfoResponse {

        private String customerType;

        private Boolean specialPriceUser;

        @JsonProperty("CUS_ID")
        private Long cusId;

        @JsonProperty("FIRSTNAME")
        private String firstName;

        @JsonProperty("LASTNAME")
        private String lastName;

        @JsonProperty("EMAIL")
        private String email;

        @JsonProperty("PHONE")
        private String phone;

        @JsonProperty("DISPLAYNAME")
        private String displayName;

        @JsonProperty("REPRESENT")
        private String represent;

        @JsonProperty("BIRTHDATE")
        private String birthDate;

        @JsonProperty("SEX")
        private String sex;

        @JsonProperty("IDENTIFIERNUMBER")
        private String identifierNumber;

        @JsonProperty("IDENTIFIERCREATEONDATE")
        private String identifierCreateOnDate;

        @JsonProperty("IDENTIFIERISSUEDBY")
        private String identifierIssuedBy;

        @JsonProperty("IDENTIFIERPHOTOPATH")
        private String identifierPhotoPath;

        @JsonProperty("PHOTOPATH")
        private String photoPath;

        @JsonProperty("PASSPORTNUMBER")
        private String passportNumber;

        @JsonProperty("PASSPORTPHOTOPATH")
        private String passportPhotoPath;

        @JsonProperty("POST_ID")
        private Long postId;

        @JsonProperty("INTRODUCTION")
        private String introduction;

        @JsonProperty("PROVINCE_ID")
        private Integer provinceId;

        @JsonProperty("DISTRICT_ID")
        private Integer districtId;

        @JsonProperty("WARDS_ID")
        private Integer wardsId;

        @JsonProperty("ADDRESS")
        private String address;

        @JsonProperty("ADDRESS_INVOICE")
        private String addressInvoice;

        @JsonProperty("LISTBANK_ID")
        private Integer listBankId;

        @JsonProperty("BANK_BRANCH")
        private String bankBranch;

        @JsonProperty("ACCOUNTNO")
        private String accountNo;

        @JsonProperty("DESCRIPTION")
        private String description;

        @JsonProperty("CREDITLIMIT")
        private Integer creditLimit;

        @JsonProperty("TAX_CODE")
        private String taxCode;

        @JsonProperty("AVATAR")
        private String avatar;

        @JsonProperty("MARKETING_STAFF")
        private Integer marketingStaff;

        @JsonProperty("CREATEDBYUSERID")
        private Integer createdByUserId;

        @JsonProperty("CREATEDONDATE")
        private String createdOnDate;

        @JsonProperty("LASTMODIFIEDBYUSERID")
        private Integer lastModifiedByUserId;

        @JsonProperty("LASTMODIFIEDONDATE")
        private String lastModifiedOnDate;

        @JsonProperty("SERVICE_DELIVERY_TYPE")
        private String serviceDeliveryType;

        @JsonProperty("EMAIL_VALIDATED")
        private Integer emailValidated;

        @JsonProperty("PHONE_VALIDATED")
        private Integer phoneValidated;

        @JsonProperty("HOME_NO_ID")
        private String homeNoId;

        @JsonProperty("HOME_NO")
        private String homeNo;

        @JsonProperty("STREET_ID")
        private String streetId;

        @JsonProperty("STREET_NAME")
        private String streetName;

        @JsonProperty("IS_SPECIAL_PRICE_USER")
        private Boolean isSpecialPriceUser;
    }
}