package com.vtp.vipo.seller.common.dto.response.order.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * Class representing a product within a package response.
 * This class contains details about the product in the package, including the product ID, SKU ID,
 * specifications, name, image, price, quantity, and associated merchant information.
 */
public class PackageProductResonse {

    /**
     * @return The unique ID of the package product
     */
    Integer id;

    /**
     * @return The ID of the product
     */
    Integer productId;

    /**
     * @return The SKU (Stock Keeping Unit) ID of the product, used for inventory management
     */
    @JsonProperty("sku_id")
    String skuId;

    /**
     * @return A list of product specifications (e.g., size, color) for the product
     */
    List<SpecResponse> spec;

    /**
     * @return The name of the product
     */
    String name;

    /**
     * @return The URL or path to the product's image
     */
    String image;

    /**
     * @return The amount or price of the product
     */
    BigDecimal productAmount;

    /**
     * @return The price of the SKU for the product
     */
    BigDecimal skuPrice;

    /**
     * @return The quantity of this product in the package
     */
    Integer quantity;

    /**
     * @return The ID of the merchant (seller) associated with this product
     */
    Integer merchantId;

    @JsonProperty("is_out_of_stock")
    Boolean isOutOfStock;

}
