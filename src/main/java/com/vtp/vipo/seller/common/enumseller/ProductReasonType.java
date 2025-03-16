package com.vtp.vipo.seller.common.enumseller;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.Translator;

/**
 * Author: hieuhm12
 * Date: 9/19/2024
 */

/**
 * Enum mô tả các lý do tạm dừng và ngừng bán sản phẩm.
 * Bao gồm:
 * - Tạm dừng bán: 0 đến 5
 * - Ngừng bán: 6 đến 10
 */
public enum ProductReasonType {
    /**
     * Tạm dừng bán: Hết hàng tạm thời (Mặc định)
     */
    TEMP_OUT_OF_STOCK(0, "product.reason.type.0"),

    /**
     * Tạm dừng bán: Cần bảo trì/sửa chữa
     */
    MAINTENANCE(1, "product.reason.type.1"),

    /**
     * Tạm dừng bán: Cần điều chỉnh giá
     */
    PRICE_ADJUSTMENT(2, "product.reason.type.2"),

    /**
     * Tạm dừng bán: Tạm dừng bán theo mùa
     */
    SEASONAL_SUSPENSION(3, "product.reason.type.3"),

    /**
     * Tạm dừng bán: Chất lượng nhà cung cấp không đảm bảo
     */
    SUPPLIER_QUALITY_ISSUE_PAUSE(4, "product.reason.type.4"),
    /**
     * Tạm dừng bán:  Khac Dừng bán:
     */
    OTHER_PAUSE(5, "product.reason.type.5"),
    /**
     * Ngừng bán: Ngừng kinh doanh (Mặc định)
     */
    BUSINESS_CLOSED(6, "product.reason.type.6"),

    /**
     * Ngừng bán: Ngừng sản xuất
     */
    PRODUCTION_STOPPED(7, "product.reason.type.7"),

    /**
     * Ngừng bán: Sản phẩm bị lỗi
     */
    PRODUCT_DEFECT(8, "product.reason.type.8"),

    /**
     * Ngừng bán: Chất lượng nhà cung cấp không đảm bảo
     */
    SUPPLIER_QUALITY_ISSUE_STOP(9, "product.reason.type.9"),

    /**
     * Khác: Lý do khác yêu cầu nhập lý do chi tiết
     */
    OTHER_STOP(10, "product.reason.type.10"),
    // Xóa sản phẩm
    WRONG_SPEC_INFO(11, "product.reason.type.11"),
    DUPLICATE_PRODUCT(12, "product.reason.type.12"),
    BUSINESS_DISCONTINUED(13, "product.reason.type.13"),
    MARKET_INCOMPATIBILITY(14, "product.reason.type.14"),
    BUSINESS_STRATEGY_CHANGE(15, "product.reason.type.15"),
    MANUFACTURER_REQUEST(16, "product.reason.type.16"),
    OTHER_DELETE(17, "product.reason.type.17"),
    /**
     * Trạng thái không xác định
     */
    UNKNOWN(-1, "unknown");
    private final int value;
    private final String description;

    ProductReasonType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return Translator.toLocale(description);
    }

    /**
     * Lấy ProductReasonType từ giá trị số.
     *
     * @param value giá trị số
     * @return ProductReasonType tương ứng
     * @throws VipoBusinessException nếu không tìm thấy lý do tương ứng
     */
    public static ProductReasonType fromValue(int value) {
        for (ProductReasonType reason : ProductReasonType.values()) {
            if (reason.getValue() == value) {
                return reason;
            }
        }
        throw new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID, String.valueOf(value));
    }
}
