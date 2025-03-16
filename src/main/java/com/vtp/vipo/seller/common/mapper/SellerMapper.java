package com.vtp.vipo.seller.common.mapper;

import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dao.entity.dto.WarehouseAddressDTO;
import com.vtp.vipo.seller.common.dao.entity.enums.ReportExportStatus;
import com.vtp.vipo.seller.common.dao.entity.projection.WarehouseAddressProjection;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.CreateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.UpdateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.request.merchant.MerchantRequestV2;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.DistrictResponse;
import com.vtp.vipo.seller.common.dto.response.ProvinceResponse;
import com.vtp.vipo.seller.common.dto.response.WardResponse;
import com.vtp.vipo.seller.common.dto.response.merchant.MerchantResponseV2;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportHistoryResponse;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.StringProcessingUtils;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportInfoRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface SellerMapper {

    WarehouseAddressEntity toWarehouseAddressEntity(CreateWarehouseAddressRequest createWarehouseAddressRequest);

    void updateWarehouseAddressEntity(
            UpdateWarehouseAddressRequest createWarehouseAddressRequest,
            @MappingTarget WarehouseAddressEntity warehouseAddressEntity
    );

    @Mapping(source = "name", target = "name", qualifiedByName = "capitalize")
    WardResponse toWardResponse(WardEntity wardEntity);

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "localDateTimeToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "localDateTimeToLong")
    WarehouseAddressDTO toWarehouseAddressDTO(WarehouseAddressEntity warehouseAddressEntity);

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "localDateTimeToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "localDateTimeToLong")
    @Mapping(source = "id", target = "id", qualifiedByName = "longToString")
    WarehouseAddressDTO toWarehouseAddressDTO(WarehouseAddressProjection warehouseAddressProjection);

    @Named("localDateTimeToLong")
    static Long formatDateTime(LocalDateTime dateTime) {
        return DateUtils.getTimeInSeconds(dateTime);
    }

    @Named("longToString")
    static String formatDateTime(Long id) {
        return String.valueOf(id);
    }

    @Named("capitalize")
    static String formatCapitalize(String value) {
        return StringProcessingUtils.capitalizeEachWord(value.toLowerCase());
    }

    ProvinceResponse toProvinceResponse(ProvinceEntity province);

    @Mapping(source = "name", target = "name", qualifiedByName = "capitalize")
    DistrictResponse toDistrictResponse(DistrictEntity districtEntity);

    @Mapping(source = "id", target = "reportId", qualifiedByName = "longToString")
    @Mapping(source = "reportSubType", target = "reportTypeDesc")
    @Mapping(source = "reportFileName", target = "reportName")
    @Mapping(source = "finishTime", target = "exportTime", qualifiedByName = "localDateTimeToLong")
    @Mapping(source = "status", target = "status", qualifiedByName = "reportExportStatusToString")
    @Mapping(source = "filePath", target = "downloadUrl")
    ReportExportHistoryResponse toReportExportHistoryResponse(ReportExportEntity reportExportEntity);

    @Named("reportExportStatusToString")
    static String reportExportStatusToString(ReportExportStatus reportExportStatus) {
        return ObjectUtils.isNotEmpty(reportExportStatus) ? reportExportStatus.toString() : null;
    }

    FinancialReportRequest toFinancialReportRequest(RevenueReportExportInfoRequest revenueReportExportInfoRequest);

    @Mapping(target = "type", ignore = true)
    MerchantResponseV2 toMerchantResponseV2(MerchantNewEntity merchantEntity);

    @Mapping(source = "phone", target = "contactPhone")
    @Mapping(source = "email", target = "contactEmail")
    @Mapping(source = "provinceId", target = "provinceId", qualifiedByName = "safeToInt")
    @Mapping(source = "districtId", target = "districtId", qualifiedByName = "safeToInt")
    @Mapping(source = "wardId", target = "wardId", qualifiedByName = "safeToInt")
    @Mapping(source = "descriptionShop", target = "description")
    @Mapping(source = "licensePicture", target = "businessLicenseImages")
    @Mapping(target = "type", ignore = true)
    void updateMerchantNew(MerchantRequestV2 merchantRequest, @MappingTarget MerchantNewEntity merchant);

    @Named("safeToInt")
    static int safeToInt(Integer value) {
        return DataUtils.safeToInt(value);
    }

    MerchantNewEntity toMerchantNewEntity(MerchantEntity merchantEntity);
}