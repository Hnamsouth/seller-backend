package com.vtp.vipo.seller.common.dto.request.address.warehouse;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.validator.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateWarehouseAddressRequest {

    /**
     * The name associated with the warehouse address. It is required and cannot be blank.
     */
    @NotBlank(message = "Tên không được để trống.")
    @Length(max = 500, message = "Tên không được dài quá 500 ký tự")
    String name;

     /**
     * The phone number associated with the warehouse address. It is required, cannot
     * be blank, and must be a valid phone number.
     *
     * @see ValidPhoneNumber for custom phone number validation
     */
    @ValidPhoneNumber
    @Length(max = 20, message = "Số điện thoại không được dài quá 20 ký tự")
    String phoneNumber;

    /**
     * The ID of the ward associated with the warehouse address. This is a required field.
     */
    @NotNull(message = "Phường không được để trống.")
    Long wardId;

    /**
     * The street associated with the warehouse address. This is an optional field.
     */
    @Length(max = 500, message = "Địa chỉ không được dài quá 500 ký tự")
    String street;

    /**
     * The detailed address associated with the warehouse address. This is a required field.
     */
    @NotBlank(message = "Địa chỉ chi tiết không được để trống.")
    @Length(max = 500, message = "Địa chỉ chi tiết không được dài quá 500 ký tự")
    String detailedAddress;

    /**
     * A flag indicating whether this address is the default address for the warehouse.
     * By default, this is set to false.
     */
    @NotNull(message = "Trường 'địa chỉ mặc định' không được để trống.")
    Boolean isDefault;

    /**
     * Set the name of the warehouse address. This method strips any leading or trailing
     * whitespace from the provided name before setting it.
     *
     * @param name the name of the warehouse address.
     */
    public void setName(String name) {
        this.name = (name == null) ? null : name.strip();  // strip leading/trailing whitespace
    }

    /**
     * Set the phone number associated with the warehouse address. This method strips any
     * leading or trailing whitespace from the provided phone number before setting it.
     *
     * @param phoneNumber the phone number of the warehouse address.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = (phoneNumber == null) ? null : phoneNumber.strip();  // strip leading/trailing whitespace
    }

    /**
     * Set the street name associated with the warehouse address. This method strips any
     * leading or trailing whitespace from the provided street name before setting it.
     *
     * @param street the street name of the warehouse address.
     */
    public void setStreet(String street) {
        this.street = (street == null) ? null : street.strip();  // strip leading/trailing whitespace
    }

    /**
     * Set the detailed address associated with the warehouse. This method strips any
     * leading or trailing whitespace from the provided detailed address before setting it.
     *
     * @param detailedAddress the detailed address of the warehouse.
     */
    public void setDetailedAddress(String detailedAddress) {
        this.detailedAddress = (detailedAddress == null) ? null : detailedAddress.strip();  // strip leading/trailing whitespace
    }

}
