package com.vtp.vipo.seller.common.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantRequest {

    private Long id;

    @NotBlank(message = "Please enter phone")
    private String phone;

    @NotBlank(message = "Please enter email")
    private String email;

    private Boolean activated;

    private Long areaId;

    private Long provinceId;

    private Long districtId;

    private Long wardId;

    @NotBlank(message = "Please enter name")
    private String name;

    private String address;

    private String contactName;

    private String paymentType;

    private String paymentName;

    private String paymentCard;

    private String paymentBank;

    private String paymentBranch;

    private String identityCard;

    private String taxCode;

    private String avatar;

    private String description;

    private String descriptionShop;

    @NotNull(message = "Please enter type")
    private Integer type;

    private Date startTimeTaxcode;

    private String addressTaxcode;

    private String licensePicture;

    private Boolean isPolicyTerms;

}
