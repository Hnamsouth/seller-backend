package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Entity representing a warehouse address.
 * <p>
 * This entity is mapped to the `warehouse_addresses` table in the database.
 * It stores the details of a warehouse address, including merchant association, address components, and
 * whether it's the default warehouse address for the merchant.
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "warehouse_address")
@Entity
public class WarehouseAddressEntity extends BaseEntity {

    /**
     * The ID of the merchant associated with the warehouse address.
     * This field is a foreign key to the `merchant` table.
     */
    @Column(nullable = false)
    private Long merchantId;

    /**
     * The full address of the warehouse, including street, ward, district, and city.
     * This is a formatted address string for convenience.
     */
    @Column(length = 500)
    private String fullAddress;

    /**
     * The name associated with the warehouse address (e.g., the name of the person or company managing the warehouse).
     */
    @Column(length = 500)
    private String name;

    /**
     * The phone number associated with the warehouse address.
     */
    @Column(length = 20)
    private String phoneNumber;

    /**
     * The ID of the ward (subdivision of the region or district) where the warehouse is located.
     * This is used for detailed address construction.
     */
    private Long wardId;

    /**
     * The street name of the warehouse's address.
     */
    @Column(length = 500)
    private String street;

    /**
     * Additional details for the warehouse address (e.g., building number, floor number).
     */
    @Column(length = 1000)
    private String detailedAddress;

    /**
     * A flag indicating whether this warehouse address is the default address for the merchant.
     * If `true`, this address is considered the default address for warehouse-related operations.
     */
    private Boolean isDefault = Boolean.FALSE;

    public void setIsDeleted(boolean isDeleted){
        super.setDeleted(isDeleted);
    }

}