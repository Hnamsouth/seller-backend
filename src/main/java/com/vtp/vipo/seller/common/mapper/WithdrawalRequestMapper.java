package com.vtp.vipo.seller.common.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.constants.WithdrawalRequestConstant;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestEntity;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestItemEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestAction;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestItemProjection;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestProjection;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.*;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import lombok.With;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface WithdrawalRequestMapper {


    @Mapping(source = "withdrawalRequestStatus", target = "withdrawalRequestStatus", qualifiedByName = "convertStatus")
    @Mapping(source = "withdrawalRequestStatus", target = "withdrawalRequestStatusDesc", qualifiedByName = "convertStatusDesc")
    @Mapping(source = ".", target = "actions", qualifiedByName = "getActionsFromStatus")
    @Mapping(source = "date", target = "date", qualifiedByName = "convertLocalDateTimeToLong")
    @Mapping(source = ".", target = "accountInfo", qualifiedByName = "convertAccountInfo")
    @Mapping(source = "withdrawalRequestType", target = "withdrawalRequestTypeDesc", qualifiedByName = "convertRequestTypDesc")
    WithdrawalRequestResponse projectionToResponse(WithdrawalRequestProjection projection);

    @Mapping(source = "withdrawalRequestStatus", target = "withdrawalRequestStatusDesc", qualifiedByName = "convertStatusDesc")
    @Mapping(source = "withdrawalRequestStatus", target = "withdrawalRequestStatus", qualifiedByName = "convertStatus")
    @Mapping(source = ".", target = "accountInfo", qualifiedByName = "convertAccountInfo")
    @Mapping(source = "date", target = "createAt", qualifiedByName = "convertLocalDateTimeToLong")
    @Mapping(source = "reCreated", target = "reCreatable", qualifiedByName = "convertIntegerToBoolean")
    WithdrawalRequestDetailResponse projectionToDetailResponse(WithdrawalRequestProjection projection);

    @Named("convertAccountInfo")
    static String convertAccountInfo(WithdrawalRequestProjection projection) {
        return String.format(WithdrawalRequestConstant.BANK_ACCOUNT_INFO,
                projection.getBankCode(),
                projection.getAccountNumber(),
                projection.getBankBranch(),
                projection.getAccountName()
        );
    }

    @Named("convertIntegerToBoolean")
    static Boolean convertIntegerToBoolean(Integer data){
        return Integer.valueOf(0).equals(data);
    }

    @Named("convertRequestTypDesc")
    static String convertRequestTypDesc(WithdrawalRequestType type){
        return type.getLabel();
    }

    @Named("convertStatusDesc")
    static String convertStatusDesc(WithdrawRequestStatusEnum status){
        return status.getLable();
    }

    @Named("convertStatus")
    static WithdrawRequestStatusEnum convertStatus(WithdrawRequestStatusEnum status){
        if(WithdrawRequestStatusEnum.APPROVED.equals(status)){
            return WithdrawRequestStatusEnum.PROCESSING;
        }
        return status;
    }

    @Named("getActionsFromStatus")
    static List<WithdrawRequestAction> getActionsFromStatus(WithdrawalRequestProjection data){
        return WithdrawRequestAction.getActionsByStatus(data);
    }

    @Named("convertLocalDateTimeToLong")
    static Long convertLocalDateTimeToLong(LocalDateTime time){
        return DateUtils.getTimeInSeconds(time);
    }

    @Mapping(source = "fees", target = "platformFees", qualifiedByName = "convertFeeMap")
    WithdrawalRequestItem convertItemProjectionToItem(WithdrawalRequestItemProjection data);

    @Named("convertFeeMap")
    static Collection<FeeMap> convertFeeMap(String fees){
        if(DataUtils.isNullOrEmpty(fees))
            return List.of();
        return JsonMapperUtils.convertJsonToObject(fees, new TypeReference<>() {
        });
    }

    @Mapping(source = "id", target = "withdrawalRequestId")
    @Mapping(source = "cancelReason", target = "message")
    WithdrawalRequestCreateRes entityToResponse(WithdrawalRequestEntity entity);

    @Mapping(source = "id", target = "withdrawalItemId")
    @Mapping(source = "withdrawAmount", target = "withdrawableAmount")
    WithdrawalRequestCreateRes.WithdrawalRequestItemCreateRes entityToResponse(WithdrawalRequestItemEntity entiry);

    Collection<WithdrawalRequestItem> convertListItemProjectionToItems(Collection<WithdrawalRequestItemProjection> data);

}
