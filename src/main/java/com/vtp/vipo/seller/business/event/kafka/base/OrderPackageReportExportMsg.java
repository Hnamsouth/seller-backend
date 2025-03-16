package com.vtp.vipo.seller.business.event.kafka.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPackageReportExportMsg {

    OrderFilterTab tabCode; // Tab đơn hàng cần xem

    String tab; // Trạng thái đơn hàng cần xem

    String orderCode; // Mã đơn hàng để lọc

    String buyerName; // Tên người mua để lọc

    String productName; // Tên sản phẩm để lọc

    String shipmentCode; // Mã vận đơn để lọc

    Long startDate; // Ngày bắt đầu để lọc đơn hàng (epoch seconds)

    Long endDate; // Ngày kết thúc để lọc đơn hàng (epoch seconds)

    List<Long> selectedOrderIds; // Danh sách ID đơn hàng cụ thể để xuất báo cáo

    String excelFileName;

    Long reportExportId;

    Long merchantId;

    Long withdrawalRequestReportId;

}