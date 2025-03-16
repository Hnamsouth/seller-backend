package com.vtp.vipo.seller.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.business.calculator.OrderPackageExportRowDTO;
import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
import com.vtp.vipo.seller.common.dao.entity.ReportExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.packageproduct.PriceRange;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderProjection;
import com.vtp.vipo.seller.common.dao.entity.projections.MyData;
import com.vtp.vipo.seller.common.dto.PackageProductDto;
import com.vtp.vipo.seller.common.dto.request.order.report.OrderPackageReportExportRequest;
import com.vtp.vipo.seller.common.dto.response.order.search.OrderListResponse;
import com.vtp.vipo.seller.common.dto.response.order.search.PackageProductResonse;
import com.vtp.vipo.seller.common.dto.response.order.search.SpecResponse;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDetailResponse;
import com.vtp.vipo.seller.common.enumseller.OrderAction;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.DateUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper used for tasks related to order entity
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "sellerOrderStatus", target = "actions", qualifiedByName = "getActionFromStatus")
    OrderListResponse orderProjectionToRes(OrderProjection projection);

    @Mapping(source = "specList", target = "spec", qualifiedByName = "convertJsonToSpecResponse")
    @Mapping(source = "isOutOfStock", target = "isOutOfStock", qualifiedByName = "convertBooleanToBoolean")
    PackageProductResonse productDtoToPackageProduct(PackageProductDto packageProductDto);

    @Named("getActionFromStatus")
    static List<OrderAction> getActionFromStatus(SellerOrderStatus status) {
        return OrderAction.getActionsFromStatus(status);
    }

    @Named("convertBooleanToBoolean")
    static Boolean convertBooleanToBoolean(Boolean status) {
        return ObjectUtils.isNotEmpty(status) ? status : Boolean.FALSE;
    }

    @Named("localDateTimeToLong")
    static Long formatDateTime(LocalDateTime dateTime) {
        return DateUtils.getTimeInSeconds(dateTime);
    }


    OrderPackageReportExportMsg toOrderPackageReportExportMsg(OrderPackageReportExportRequest orderPackageReportExportRequest);

    @Named("convertJsonToSpecResponse")
    static List<SpecResponse> convertJsonToSpecResponse(String data) {
        Map<String, String> specs = JsonMapperUtils.convertJsonToObject(data, Map.class);
        // If the specs list is not empty, convert each spec into an OrderListRes.PackageProduct.Spec
        if (!CollectionUtils.isEmpty(specs)) {
            List<SpecResponse> spec = new ArrayList<>();
            specs.forEach((key, value) -> spec.add(new SpecResponse(key, value)));
            return spec;
        }
        return new ArrayList<>();
    }

    @Mapping(source = "createTime", target = "createTime", qualifiedByName= "longToLocalDateTime")
    @Mapping(source = "deliveryStartTime", target = "deliveryStartTime", qualifiedByName= "longToLocalDateTime")
    @Mapping(source = "receiverName", target = "receiverName", qualifiedByName = "hideReceiverNameInfo")
    @Mapping(source = "receiverPhone", target = "receiverPhone", qualifiedByName = "hideReceiverPhoneInfo")
    @Mapping(source = "priceRanges", target = "priceRanges", qualifiedByName = "convertStrToPriceRanges")
    @Mapping(source = "deliverySuccessTime", target = "deliverySuccessTime", qualifiedByName = "longToLocalDateTime")
    OrderPackageExportRowDTO toOrderPackageExportRowDTOPrimary(MyData projection);

    @Named("longToLocalDateTime")
    static LocalDateTime longToLocalDateTime(Long timestamp) {
        return ObjectUtils.isNotEmpty(timestamp) && timestamp > 0 ?
                DateUtils.convertEpochSecondsToLocalDateTime(timestamp) : null;
    }

    @Named("hideReceiverNameInfo")
    static String hideReceiverNameInfo(String value) {
        return ObjectUtils.isNotEmpty(value) ? DataUtils.hideStringValue(value, 1, 1) : null;
    }

    @Named("hideReceiverPhoneInfo")
    static String hideReceiverPhoneInfo(String value) {
        return ObjectUtils.isNotEmpty(value) ? DataUtils.hideStringValue(value, 0, 2) : null;
    }

    @Named("convertStrToPriceRanges")
    static List<PriceRange> convertStrToPriceRanges(String priceRangeStr) {
        if (StringUtils.isBlank(priceRangeStr))
            return List.of();
        return JsonMapperUtils.convertJsonToObject(priceRangeStr, new TypeReference<List<PriceRange>>() {});
    }

    @Mapping(target = "orderCode", ignore = true)
    @Mapping(target = "shipmentCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "deliveryStartTime", ignore = true)
    @Mapping(target = "sellerOrderStatusName", ignore = true)
    @Mapping(target = "noteSeller", ignore = true)
    @Mapping(target = "expectedDeliverySuccessDate", ignore = true)
    @Mapping(target = "shipmentDate", ignore = true)
    @Mapping(target = "deliverySuccessTime", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "categoryCode", ignore = true)
    @Mapping(target = "negotiatedAmount", ignore = true)
    @Mapping(target = "refCode", ignore = true)
    @Mapping(target = "totalPlatformFee", ignore = true)
    @Mapping(target = "prepayment", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    @Mapping(target = "receiverPhone", ignore = true)
    @Mapping(target = "receiverWard", ignore = true)
    @Mapping(target = "receiverDistrict", ignore = true)
    @Mapping(target = "receiverProvince", ignore = true)
    @Mapping(target = "receiverAddress", ignore = true)
    @Mapping(target = "carrierName", ignore = true)
    @Mapping(target = "shipmentMethod", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "countryName", ignore = true)
    @Mapping(target = "totalDomesticShippingFee", ignore = true)
    @Mapping(target = "totalSkuPrice", ignore = true)
    @Mapping(target = "originTotalSkuPrice", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "totalShippingFee", ignore = true)
    @Mapping(source = "priceRanges", target = "priceRanges", qualifiedByName = "convertStrToPriceRanges")
    @Mapping(target = "sellerNoteForShipment", ignore = true)
    @Mapping(target = "expectedDeliveryTime", ignore = true)
    OrderPackageExportRowDTO toOrderPackageExportRowDTOOther(MyData projection);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    OrderPackageExportRowDTO updateOrderPackageExportRowDTO(
            OrderPackageExportRowDTO sumary,
            @MappingTarget OrderPackageExportRowDTO target
    );

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "localDateTimeToLong")
    @Mapping(source = "finishTime", target = "finishTime", qualifiedByName = "localDateTimeToLong")
    ReportExportDetailResponse toReportExportDetailResponse(ReportExportEntity reportExportEntity);

}
