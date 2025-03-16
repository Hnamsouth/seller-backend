package com.vtp.vipo.seller.common.dao.entity.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Enum representing the possible statuses of a buyer's order.
 * Each status has a unique code and a human-readable name.
 *
 * Equivalent to order_package.orderStatus field
 */
@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum BuyerOrderStatus {

    WAIT_FOR_PAY_ORDER("011", "Đơn hàng đang chờ thanh toán"),
    WAIT_FOR_ADJUST_PRICE("017", "Đơn hàng đang chờ điều chỉnh giá"),
    WAITING_FOR_SELLER_CONFIRMATION("021", "Đơn chờ nhà bán xác nhận"),
    SUPPLIER_IS_PREPARING_ORDER("030", "Nhà bán đang chuẩn bị hàng"),
    SUPPLIER_IS_PREPARING_ORDER_PENDING("032", "Nhà bán chuẩn bị hàng thành công (chưa có mã vận chuyển)"),
    SUPPLIER_IS_PREPARING_ORDER_SUCCESS("103", "Nhà bán chuẩn bị hàng thành công (có mã vận chuyển)"),
    ORDER_CANCELLED_BY_PAYMENT_OVERDUE("091", "Đơn hàng quá hạn thanh toán, đã được hủy bởi VIPO"),
    ORDER_CANCELLED_BY_CUSTOMER("092", "Đơn hàng được hủy bởi KH"),
    ORDER_HAD_BEEN_DELIVERIED_SUCCESSFULLY("501", "Giao hàng thành công")
    ;


    String orderStatusCode;
    String orderStatusName;

    // Static map for quick lookup by orderStatusCode
    private static final Map<String, BuyerOrderStatus> CODE_MAP = new HashMap<>();

    public static final List<String> ALLOW_TO_MODIFY_PRODUCT_BUYER_ORDER_STATUSES;

    // Static block to populate the map with the status codes and corresponding BuyerOrderStatus
    static {
        for (BuyerOrderStatus status : BuyerOrderStatus.values()) {
            CODE_MAP.put(status.getOrderStatusCode(), status);
        }

        ALLOW_TO_MODIFY_PRODUCT_BUYER_ORDER_STATUSES
                = Stream.of(
                        ORDER_CANCELLED_BY_PAYMENT_OVERDUE,
                        ORDER_CANCELLED_BY_CUSTOMER,
                        ORDER_HAD_BEEN_DELIVERIED_SUCCESSFULLY
                ).map(BuyerOrderStatus::getOrderStatusCode).toList();
    }

    /**
     * Returns the BuyerOrderStatus corresponding to the given orderStatusCode.
     *
     * @param code the order status code to match
     * @return the matching BuyerOrderStatus
     * @throws IllegalArgumentException if no matching status is found
     */
    public static BuyerOrderStatus fromOrderStatusCode(String code) {
        // Look up the status by order status code
        BuyerOrderStatus status = CODE_MAP.get(code);

        if (status == null) {
            // If no matching status is found, throw an exception
            throw new IllegalArgumentException("No matching BuyerOrderStatus for code: " + code);
        }

        return status;
    }
}