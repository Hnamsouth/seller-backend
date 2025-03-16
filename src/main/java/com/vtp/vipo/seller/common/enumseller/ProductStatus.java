package com.vtp.vipo.seller.common.enumseller;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.Translator;

/**
 * Author: hieuhm12
 * Date: 9/15/2024
 */

/**
 * Enum mô tả các trạng thái của sản phẩm.
 * Trạng thái bao gồm:
 * 0: Đang tạo sản phẩm
 * 1: Chờ kiểm duyệt tạo
 * 2: Đã kiểm duyệt tạo
 * 3: Từ chối duyệt tạo
 * 4: Đang bán
 * 5: Tạm dừng bán
 * 6: Ngừng bán
 * 7: Đang sửa sản phẩm
 * 8: Chờ kiểm duyệt sửa
 * 9: Đã kiểm duyệt sửa
 * 10: Từ chối duyệt sửa
 * 11: Đã hủy
 * 12: Đã bị khóa
 */
public enum ProductStatus {
    /**
     * Trạng thái 0: Đang tạo sản phẩm
     */
    NEW(0, "product.status.0"),

    /**
     * Trạng thái 1: Chờ kiểm duyệt tạo
     */
    PENDING(1, "product.status.1"),

    /**
     * Trạng thái 2: Đã kiểm duyệt tạo
     */
    APPROVED(2, "product.status.2"),

    /**
     * Trạng thái 3: Từ chối duyệt tạo
     */
    REJECT(3, "product.status.3"),

    /**
     * Trạng thái 4: Đang bán
     */
    SELLING(4, "product.status.4"),

    /**
     * Trạng thái 5: Tạm dừng bán
     */
    PAUSED(5, "product.status.5"),

    /**
     * Trạng thái 6: Ngừng bán
     */
    STOPPED(6, "product.status.6"),

    /**
     * Trạng thái 7: Đang sửa sản phẩm
     */
    EDITING(7, "product.status.7"),

    /**
     * Trạng thái 8: Chờ kiểm duyệt sửa
     */
    ADJUST_PENDING(8, "product.status.8"),

    /**
     * Trạng thái 9: Đã kiểm duyệt sửa
     */
    ADJUST_APPROVED(9, "product.status.9"),

    /**
     * Trạng thái 10: Từ chối duyệt sửa
     */
    ADJUST_REJECT(10, "product.status.10"),

    /**
     * Trạng thái 11: Đã hủy
     */
    CANCELED(11, "product.status.11"),
    /**
     * Trạng thái 1s: Đã bị khoa
     */
    LOCKED(12, "product.status.12"),
    UNKNOWN(-1, "unknown");

    private final int value;
    private final String description;

    ProductStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Lấy giá trị của trạng thái.
     *
     * @return giá trị số nguyên đại diện cho trạng thái
     */
    public int getValue() {
        return value;
    }

    /**
     * Lấy mô tả của trạng thái.
     *
     * @return mô tả  của trạng thái
     */
    public String getDescription() {
        return Translator.toLocale(description);
    }

    /**
     * Tìm kiếm trạng thái sản phẩm dựa trên giá trị.
     *
     * @param value giá trị số nguyên của trạng thái
     * @return trạng thái sản phẩm tương ứng
     * @throws VipoBusinessException nếu không tìm thấy trạng thái tương ứng
     */
    public static ProductStatus fromValue(int value) {
        for (ProductStatus status : ProductStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID, String.valueOf(value));
    }
}

