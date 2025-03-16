package com.vtp.vipo.seller.retryservice.mapper;

import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.enumseller.OrderAction;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.config.mq.kafka.RetriesMessageData;
import com.vtp.vipo.seller.retryservice.dao.entity.RetriesMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RetryMessageMapper {

    @Mapping(source = "retryMessageId", target = "messageId")
    @Mapping(source = "data", target = "data", qualifiedByName = "convertDataToString")
    RetriesMessage toRetriesMessage(RetriesMessageData retryMessageDTO);

    @Named("convertDataToString")
    static String convertDataToString( MessageData data) {
        return JsonMapperUtils.writeValueAsString(data);
    }

    @Mapping(target = "messageId", ignore = true)
    @Mapping(source = "messageId", target = "retryMessageId")
    @Mapping(source = "data", target = "data", qualifiedByName = "convertStringToData")
    RetriesMessageData toRetriesMessageData(RetriesMessage retriesMessage);

    @Named("convertStringToData")
    static MessageData convertStringToData(String data) {
        return JsonMapperUtils.convertJsonToObject(data, MessageData.class);
    }

}
