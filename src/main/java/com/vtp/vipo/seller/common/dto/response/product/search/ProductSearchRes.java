package com.vtp.vipo.seller.common.dto.response.product.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vtp.vipo.seller.common.enumseller.ProductStatus;
import com.vtp.vipo.seller.common.utils.DataUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Author: hieuhm12
 * Date: 9/17/2024
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRes {
    /*id bang product*/
    Long id;
    /*Mã sản phẩm*/
    String productCode;
    /*Tên sản phẩm*/
    String productName;
    /*anh*/
    String image;
    /*Số phân loại
    * mặc dù biến tên là countClassify, tuy nhiên ta sẽ count bảng seller_attribute @@ */
    Long countClassify;
    /*Số lượng SKU*/
    Long countSku;
    /*Loại giá*/
    Integer priceType;
    /*Đơn giá hiển thịt*/
    BigDecimal price;
    /*Ngành hàng*/
    String category;
    /*Đã bán*/
    Long sold;
    /*id Tồn kho thực tế*/
    Long realStock;
    /*Chờ bán*/
    Long numProdWaitingSold;
    /*Tồn kho khả dụng*/
    Long numProdAvailable;
    /*Trạng thái sản phẩm*/
    Integer status;

    BigDecimal platformDiscountRate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant createdTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant updatedTime;

    public Long getNumProdAvailable() {
        long result = realStock - numProdWaitingSold;
        return Math.max(result, 0L);
    }

    //todo: remove the use of the constructor, I just tryna fix the bug as soon as possible
    public ProductSearchRes(
            Long id, String productCode, String name, String image,
            Long sellerAttributeCount, Long productSellerSkuCount,
            Integer productPriceType, BigDecimal price, String categoryPath,
            Long buyCount, Long totalStock, Long numProdWaitingSold, ProductStatus status,
            BigDecimal platformDiscountRate, Long createTime, Long updateTime
    ) {
        this.id = id;
        this.productCode = productCode;
        this.productName = name;
        this.image = image;
        this.countClassify = sellerAttributeCount;
        this.countSku = productSellerSkuCount;
        this.priceType = productPriceType;
        this.price = price;
        this.category = categoryPath;
        this.sold = buyCount;
        this.realStock = totalStock;
        this.numProdWaitingSold = numProdWaitingSold;
        this.status = status.getValue();
        this.platformDiscountRate = platformDiscountRate;
        this.createdTime = Instant.ofEpochSecond(createTime);
        this.updatedTime = Instant.ofEpochSecond(updateTime);
    }
}
