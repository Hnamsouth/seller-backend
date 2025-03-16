package com.vtp.vipo.seller.common.dto.request.order.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderShipmentEntity;
import com.vtp.vipo.seller.common.dao.entity.PackageProductEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the request payload for exporting
 * order package reports with various filtering criteria.
 * <p>
 * This class captures the parameters required to generate a comprehensive
 * report of order packages based on specific filters such as order status,
 * order code, buyer name, product name, shipment code, and a range of creation dates.
 * Additionally, it allows selecting specific order IDs for targeted report generation.
 * </p>
 *
 * @author anhdev
 * @version 1.0
 * @since 2024-04-27
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderPackageReportExportRequest {

    OrderFilterTab tabCode;

    /**
     * Specifies the tab or category of order statuses to view in the report.
     *
     * It is equivalent to the parent status in the SellerOrderStatus
     *
     * @see SellerOrderStatus#getParentSellerOrderSattus()
     */
    String tab; // Trạng thái đơn hàng cần xem

    /**
     * Filters the report to include only orders matching the specified order code.
     * <p>
     * This field allows partial or full matching of the order code using
     * wildcard characters. It is case-insensitive and supports substring searches.
     * </p>
     *
     * @see OrderPackageEntity#getOrderCode()
     */
    String orderCode; // Mã đơn hàng để lọc

    /**
     * Filters the report to include only orders placed by buyers with the specified name.
     * <p>
     * This field supports partial or full matching of the buyer's name using
     * wildcard characters. It is case-insensitive and facilitates substring searches.
     * </p>
     *
     * @see OrderPackageEntity#getReceiverName()
     */
    String buyerName; // Tên người mua để lọc

    /**
     * Filters the report to include only orders containing products with the specified name.
     * <p>
     * This field allows partial or full matching of the product name using
     * wildcard characters. It is case-insensitive and supports substring searches.
     * </p>
     *
     * @see PackageProductEntity#getName()
     */
    String productName; // Tên sản phẩm để lọc

    /**
     * Filters the report to include only shipments matching the specified shipment code.
     * <p>
     * This field supports partial or full matching of the shipment code using
     * wildcard characters. It is case-insensitive and enables substring searches.
     * </p>
     *
     * @see OrderShipmentEntity#getShipmentCode()
     */
    String shipmentCode; // Mã vận đơn để lọc

    /**
     * Defines the start date for filtering orders based on their creation time.
     * <p>
     * The start date is specified in epoch seconds (the number of seconds since
     * 00:00:00 UTC on January 1, 1970). Only orders created on or after this date
     * will be included in the report.
     * </p>
     *
     * @see OrderPackageEntity#getCreateTime()
     */
    Long startDate; // Ngày bắt đầu để lọc đơn hàng (epoch seconds)

    /**
     * Defines the end date for filtering orders based on their creation time.
     * <p>
     * The end date is specified in epoch seconds (the number of seconds since
     * 00:00:00 UTC on January 1, 1970). Only orders created on or before this date
     * will be included in the report.
     * </p>
     *
     * @see OrderPackageEntity#getCreateTime()
     */
    Long endDate; // Ngày kết thúc để lọc đơn hàng (epoch seconds)

    /**
     * A list of specific order IDs to include in the report.
     * <p>
     * When provided, the report will only include orders that have their IDs
     * present in this list, overriding other filtering criteria related to IDs.
     * </p>
     *
     * @see OrderPackageEntity#getId()
     *
     * @todo Ask BA for the maximum allowed size of this list.
     */
    @Size(max = 200)
    List<Long> selectedOrderIds; // Danh sách ID đơn hàng cụ thể để xuất báo cáo

    /**
     * Use for storing excel file name for this request
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String excelFileName;

    /**
     * use for storing the report_export.id
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long reportExportId;
}