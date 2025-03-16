package com.vtp.vipo.seller.common.dao.entity.projection;

import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;

import java.math.BigDecimal;

/**
 * Interface representing an order in the system.
 * Provides methods to retrieve detailed information about the order.
 */
public interface OrderProjection {

    /**
     * @return ID của đơn hàng
     */
    Long getId();

    /**
     * @return Mã đơn hàng
     */
    String getOrderCode();

    /**
     * @return Trạng thái của đơn hàng
     */
    String getOrderStatus();

    /**
     * @return ID khách hàng
     */
    Integer getCustomerId();

    /**
     * @return Tên khách hàng
     */
    String getCustomerName();

    /**
     * @return Avatar của khách hàng
     */
    String getCustomerAvatar();

    /**
     * @return Tổng số tiền thanh toán
     */
    BigDecimal getTotalPayment();

    /**
     * @return Tổng số tiền đã thanh toán
     */
    BigDecimal getTotalPaid();

    /**
     * @return Số tiền đã thanh toán trước
     */
    BigDecimal getPrePayment();

    /**
     * @return Thời gian cập nhật đơn hàng (timestamp)
     */
    Long getUpdatedAt();

    /**
     * @return Thời gian tạo đơn hàng (timestamp)
     */
    Long getCreatedAt();

    /**
     * @return Tổng giá trị sản phẩm trong đơn hàng
     */
    BigDecimal getProductAmount();

    /**
     * @return Lý do hủy đơn hàng (nếu có)
     */
    String getCancelNote();

    /**
     * @return Thời gian thanh toán (timestamp)
     */
    Long getPaymentTime();

    /**
     * @return Thời gian giao hàng thành công (timestamp)
     */
    Long getDeliverySuccessTime();

    /**
     * @return Thời gian hủy đơn hàng (timestamp)
     */
    Long getCancelTime();

    /**
     * @return Mã vận đơn (shipment code)
     */
    String getShipmentCode();

    String getShipmentMessage();

    /**
     * @return Thời gian giao hàng (timestamp)
     */
    Long getShipmentTime();

    /**
     * @return Thời gian trả hàng (timestamp)
     */
    Long getReturnTime();

    /**
     * @return Trạng thái đơn hàng của người bán
     */
    SellerOrderStatus getSellerOrderStatus();

    /**
     * @return Mô tả trạng thái đơn hàng của người bán
     */
    String getSellerOrderStatusDesc();

    /**
     * @return Danh sách các sản phẩm trong đơn hàng dạng gói
     */
    String getPackageProductList();

    /**
     * @return Số tiền trả lại cho khách hàng
     */
    BigDecimal getReturnAmount();

    /**
     * @return Số tiền hoàn lại cho khách hàng
     */
    BigDecimal getRefundAmount();

    /**
     * @return Cờ cho biết giá sản phẩm có thay đổi không
     */
    Integer getIsChangePrice();

    Integer getIsPrinted();

    String getShipmentStatus();

    /**
     * @return Số lượng sản phẩm trong đơn hàng
     */
    Integer getQuantityProduct();
}
