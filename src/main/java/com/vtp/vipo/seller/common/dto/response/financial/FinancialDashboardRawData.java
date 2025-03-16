package com.vtp.vipo.seller.common.dto.response.financial;

import java.math.BigDecimal;

public interface FinancialDashboardRawData {
    // tổng số đơn
    long getTotalOrders();

    // tổng số KH đã đặt hàng thành công (trạng thái chờ nhà bán xác nhận)
    long getTotalBuyers();

    // số đơn chuyển đổi
    long getConvertedOrders();

    // số đơn giao thành công (trạng thái đã giao)
    long getDeliveredOrders();

    // số đơn hoàn/hủy
    long getReturnCancelOrders();

    // doanh thu = Tổng tiền hàng các đơn hàng
    BigDecimal getRevenue();

    // phí sàn: Tổng tiền phí sàn của các đơn hàng (Cấu hình bên CMS) với các đơn giao hàng thành công
    BigDecimal getPlatformFee();

    // chi phí khác: Tổng tiền các chi phí khác của các đơn hàng (Cấu hình bên CMS) với các đơn giao hàng thành công
    BigDecimal getOtherFee();

    // tổng đàm phán: Tính trên các đơn hàng đã giao thành công
    // Tiền đàm phán theo tổng tiền đơn hàng = tổng số tiền đàm phán của các đơn hàng
    // Tiền đàm phán theo từng SKU =  tổng số tiền hàng chênh lệch sau đàm phán của các đơn hàng
    BigDecimal getPriceNegotiated();

    default String show() {
        return "FinancialDashboardRawData{" +
                "totalOrders=" + getTotalOrders() +
                ", totalBuyers=" + getTotalBuyers() +
                ", convertedOrders=" + getConvertedOrders() +
                ", deliveredOrders=" + getDeliveredOrders() +
                ", returnCancelOrders=" + getReturnCancelOrders() +
                ", revenue=" + getRevenue() +
                ", platformFee=" + getPlatformFee() +
                ", otherFee=" + getOtherFee() +
                ", priceNegotiated=" + getPriceNegotiated() +
                '}';
    }
}
