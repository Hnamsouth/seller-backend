package com.vtp.vipo.seller.financialstatement.event.kafka;

import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.mq.kafka.DLQMessageData;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.config.mq.kafka.MessageListener;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportMsg;
import com.vtp.vipo.seller.services.financial.FinancialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RevenueReportExportConsumer extends MessageListener<RevenueReportExportMsg>  {

    @Value("${custom.properties.kafka.topic.vipo-seller-revenue-export-dlq.name}")
    private String revenueReportExportDLQTopicName;

    private final FinancialService financialService;

    @KafkaListener(
            topics = "${custom.properties.kafka.topic.vipo-seller-revenue-export.name}",
            groupId = "${custom.properties.messaging.kafka.groupId}",
            concurrency = "${custom.properties.kafka.topic.vipo-seller-revenue-export.concurrent.thread}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void reportExportEventListener(
            String data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment) {
        super.messageListener(data, topic, partition, offset, acknowledgment, 0, 0, revenueReportExportDLQTopicName);
    }

    @KafkaListener(
            topics = "${custom.properties.kafka.topic.vipo-seller-order-package-export-excel-retries.name}",
            groupId = "${custom.properties.messaging.kafka.groupId}",
            concurrency = "1",
            containerFactory = "kafkaListenerContainerFactory")
    public void reportExportRetriesListener(
            String data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment) {
        super.messageRetriesListener(data, topic, partition, offset, acknowledgment);
    }

    @KafkaListener(
            topics = "${custom.properties.kafka.topic.vipo-seller-order-package-export-excel-dlq.name}",
            groupId = "${custom.properties.messaging.kafka.groupId}",
            concurrency = "1",
            containerFactory = "kafkaListenerContainerFactory")
    public void reportExportDLQListener(
            String data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment) {
        super.messageDLQListener(data, topic, partition, offset, acknowledgment);
    }

    @Override
    protected void handleMessageEvent(String topic, String partition, String offset, MessageData<RevenueReportExportMsg> input) {
        log.info("Handle revenue pdf export: {}", JsonMapperUtils.writeValueAsString(input.getContent()));
        try {
            financialService.exportRevenueReport(input.getContent());
        } catch (VipoInvalidDataRequestException e) {
            log.warn("Error when handle revenue pdf export: {}", e.getMessage());
            //todo: handle the case the message is not expected
        }

    }

    @Override
    protected void handleDeadMessageEvent(String topic, String partition, String offset, DLQMessageData<RevenueReportExportMsg> input) {

    }
}
