package com.vtp.vipo.seller.common.enumseller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Enum describes that values in the column product.productPriceType
 * <p>
 * 0 Đồng giá SKU không thang giá:  no price range apply
 * 1 Đồng giá SKU có thang giá:     use product price range
 * 2 Đơn giá theo SKU:              use sku price range
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ProductPriceTypeEnum {

    NO_PRICE_RANGE(0),
    PRODUCT_PRICE_RANGE(1),
    SKU_PRICE(2);

    Integer value;
}
