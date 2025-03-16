package com.vtp.vipo.seller.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * DTO (Data Transfer Object) class representing a product within a package.
 * This class contains various details about the product, including product ID, SKU ID,
 * specifications, name, image, price, quantity, and associated merchant information.
 * It is used to transfer product data between layers or services in the system.
 */
public class PackageProductDto {

    /**
     * @return The unique ID of the package product
     */
    private Integer id;

    /**
     * @return The ID of the product
     */
    private Integer productId;

    /**
     * @return The SKU (Stock Keeping Unit) ID of the product, used for inventory management
     */
    private String skuId;

    private String lazbaoSkuId;

    /**
     * @return A list of product specifications (e.g., color, size) in string format
     */
    private String specList;

    /**
     * @return The name of the product
     */
    private String name;

    /**
     * @return The URL or path to the product's image
     */
    private String image;

    /**
     * @return The amount or price of the product
     */
    private Double productAmount;

    /**
     * @return The price of the SKU in string format
     */
    private String skuPrice;

    /**
     * @return The quantity of the product in the package
     */
    private Integer quantity;

    /**
     * @return The ID of the merchant (seller) associated with this product
     */
    private Integer merchantId;

    Boolean isOutOfStock;

}

