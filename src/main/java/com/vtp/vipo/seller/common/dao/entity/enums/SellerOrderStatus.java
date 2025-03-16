package com.vtp.vipo.seller.common.dao.entity.enums;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Enum representing the status of a seller's order in the order package.
 * This is used in the 'sellerOrderStatus' column of the 'order_package' table in the database.
 */
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum SellerOrderStatus {

    PARENT_WAITING_PAYMENT("Chờ thanh toán", null),
    PARENT_WAITING_APPROVAL("Chờ duyệt", null),
    PARENT_WAITING_SHIPMENT("Chờ giao hàng", null),
    PARENT_IN_TRANSIT("Đang giao hàng", null),
    PARENT_DELIVERED("Đã giao hàng", null),
    PARENT_CANCELLED("Đã hủy đơn hàng", null),

    /**
     * Order is awaiting payment from the buyer.
     */
    WAITING_FOR_PAYMENT("Chờ thanh toán", PARENT_WAITING_PAYMENT),
    /**
     * The price of the order has been adjusted by the seller.
     */
    ORDER_PRICE_ADJUSTED("Đơn giá đã được điều chỉnh", PARENT_WAITING_PAYMENT),

    /**
     * The seller is waiting for confirmation from the buyer.
     * chờ nhà bán xác nhận
     */
    WAITING_FOR_SELLER_CONFIRMATION("Chờ xác nhận từ người bán", PARENT_WAITING_APPROVAL),

    /**
     * The seller has rejected the order.
     */
    SELLER_REJECTED_ORDER("Người bán từ chối đơn hàng", PARENT_CANCELLED),

    /**
     * The seller has cancelled the order.
     */
    ORDER_CANCELLED_BY_SELLER("Đơn hàng bị hủy bởi Nhà bán", PARENT_CANCELLED),

    /**
     * The customer has cancelled the order.
     */
    ORDER_CANCELLED_BY_CUSTOMER("Đơn hàng bị hủy bởi khách hàng", PARENT_CANCELLED),

    /**
     * The order is awaiting preparation by the seller.
     */
    WAITING_FOR_ORDER_PREPARATION("Chờ chuẩn bị hàng", PARENT_WAITING_SHIPMENT),

    /**
     * The order has been prepared by the seller.
     */
    ORDER_PREPARED("Đã chuẩn bị hàng", PARENT_WAITING_SHIPMENT),

    /**
     * The shipment connection has been successfully established.
     */
    ORDER_SHIPMENT_CONNECTION_SUCCESS("Kết nối vận chuyển thành công", PARENT_WAITING_SHIPMENT),

    /**
     * The order has been delivered to the shipping company.
     */
    ORDER_DELIVERED_TO_SHIPPING("Đơn hàng đã được giao cho vận chuyển", PARENT_IN_TRANSIT),

    /**
     * The order is currently in transit to the destination.
     */
    ORDER_IN_TRANSIT("Đơn hàng đang vận chuyển", PARENT_IN_TRANSIT),

    /**
     * The order has been successfully completed and delivered.
     */
    ORDER_COMPLETED("Đơn hàng đã hoàn thành", PARENT_DELIVERED),

    ORDER_CANCELLED_BY_VTP("Đơn hàng bị hủy bởi VTP", PARENT_CANCELLED),

    ORDER_CANCELLED_BY_VIPO("Đơn hàng bị hủy bởi VIPO", PARENT_CANCELLED)

    ;
    String description;

    SellerOrderStatus parentSellerOrderSattus;

    public final static List<SellerOrderStatus> PARENT_SELLER_ORDER_STATUSES
            = List.of(
            PARENT_WAITING_PAYMENT,
            PARENT_WAITING_APPROVAL,
            PARENT_WAITING_SHIPMENT,
            PARENT_IN_TRANSIT,
            PARENT_DELIVERED,
            PARENT_CANCELLED
    );

    public final static List<String> PARENT_SELLER_ORDER_STATUS_STR_LIST = new ArrayList<>();

    public final static Map<SellerOrderStatus, List<SellerOrderStatus>> PARENT_TO_CHILDREN_SELLER_ORDER_STATUS
            = new HashMap<>();

    static {
        for (SellerOrderStatus sellerOrderStatus : PARENT_SELLER_ORDER_STATUSES) {
            PARENT_SELLER_ORDER_STATUS_STR_LIST.add(sellerOrderStatus.name());
        }

        for (SellerOrderStatus sellerOrderStatus : SellerOrderStatus.values()) {
            SellerOrderStatus parentStatus = sellerOrderStatus.getParentSellerOrderSattus();
            if (ObjectUtils.isNotEmpty(parentStatus)) {
                List<SellerOrderStatus> childrenStatuses = PARENT_TO_CHILDREN_SELLER_ORDER_STATUS.computeIfAbsent(parentStatus, k -> new ArrayList<>());
                childrenStatuses.add(sellerOrderStatus);
            }
        }
    }

    public static SellerOrderStatus getByName(String value) {
        if (StringUtils.isBlank(value))
            return null;
        String stripValue = value.strip();
        for (SellerOrderStatus sellerOrderStatus : SellerOrderStatus.values()) {
            if (sellerOrderStatus.name().equals(stripValue))
                return sellerOrderStatus;
        }
        return null;
    }

    public static List<SellerOrderStatus> getSellerOrderStatusCanceled(){
        return List.of(
                SELLER_REJECTED_ORDER,
                ORDER_CANCELLED_BY_SELLER,
                ORDER_CANCELLED_BY_CUSTOMER
        );
    }

}


