package com.vtp.vipo.seller.common.dao.entity.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dto.response.WardResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Data Transfer Object (DTO) for representing a warehouse address.
 * <p>
 * This DTO is used to transfer warehouse address data between layers of the application.
 * It is typically used for returning data in API responses.
 * </p>
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WarehouseAddressDTO {

    /**
     * Unique identifier for the warehouse address.
     */
    String id;

    /**
     * Merchant ID associated with this warehouse address.
     * This is typically used to link the address to a specific merchant.
     */
    Long merchantId;

    /**
     * The full formatted address, which combines all the address fields (street, ward, etc.).
     */
    String fullAddress;

    /**
     * The name of the person or entity associated with the warehouse address.
     */
    String name;

    /**
     * The phone number associated with the warehouse address.
     */
    String phoneNumber;

    /**
     * The ward response object containing the information about the ward this address belongs to.
     */
    WardResponse wardResponse;

    /**
     * The street part of the warehouse address.
     */
    String street;

    /**
     * The detailed address part, which may include specific building numbers, floor, etc.
     */
    String detailedAddress;

    /**
     * Boolean flag indicating if this address is the default address for the merchant.
     * A value of `true` indicates it is the default.
     */
    Boolean isDefault;

    /**
     * Timestamp indicating when this warehouse address was created (in Unix timestamp format).
     */
    private Long createdAt;

    /**
     * Timestamp indicating when this warehouse address was last updated (in Unix timestamp format).
     */
    private Long updatedAt;

}