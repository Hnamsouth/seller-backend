package com.vtp.vipo.seller.business.event.kafka;

import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.mq.kafka.DLQMessageData;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.config.mq.kafka.MessageListener;
import com.vtp.vipo.seller.services.WithdrawalRequestService;
import com.vtp.vipo.seller.services.order.ReportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReportExportConsumer extends MessageListener<OrderPackageReportExportMsg> {

    private final ReportExportService reportExportService;

    private final WithdrawalRequestService withdrawalRequestService;

    @Value("${custom.properties.kafka.topic.vipo-seller-order-package-export-excel-dlq.name}")
    private String orderPackageExportDLQTopicName;

    @KafkaListener(
            topics = "${custom.properties.kafka.topic.vipo-seller-order-package-export-excel.name}",
            groupId = "${custom.properties.messaging.kafka.groupId}",
            concurrency = "${custom.properties.kafka.topic.vipo-seller-order-package-export-excel.concurrent.thread}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void reportExportEventListener(
            String data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment) {
        super.messageListener(data, topic, partition, offset, acknowledgment, 2, 5, orderPackageExportDLQTopicName);
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
    protected void handleMessageEvent(
            String topic, String partition, String offset, MessageData<OrderPackageReportExportMsg> input
    ) {
        log.info("Order Packages report: {}", JsonMapperUtils.writeValueAsString(input.getContent()));
        try {
            reportExportService.exportOrderPackageReport(input.getContent());
        } catch (VipoInvalidDataRequestException exception) {
            log.warn(
                    "Order Packages report: Invalid data: for topic {}, partition {}, offset {}, message {} with message {}",
                    topic, partition, offset, JsonMapperUtils.writeValueAsString(input), exception.getMessage()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleDeadMessageEvent(
            String topic, String partition, String offset, DLQMessageData<OrderPackageReportExportMsg> input
    ) {
        try {
            if (
                    ObjectUtils.isNotEmpty(input)
                            && ObjectUtils.isNotEmpty(input.getData())
                            && ObjectUtils.isNotEmpty(input.getData().getContent())
            ) {
                OrderPackageReportExportMsg exportMsg = input.getData().getContent();
                if (ObjectUtils.isNotEmpty(exportMsg.getReportExportId()))
                    reportExportService.markExportReportAsFailed(exportMsg, input.getFailedMessage());
                else
                    withdrawalRequestService.markExportAsFailed(exportMsg, input.getFailedMessage());
            }
        } catch (VipoInvalidDataRequestException vipoInvalidDataRequestException) {
            log.info("[KafkaConsumer][{}][{}][{}] Exception: Kafka DLQ Exception with the content {} and " +
                            "the exception message {}",
                    topic, partition, offset,
                    JsonMapperUtils.writeValueAsString(input), vipoInvalidDataRequestException.getMessage());
        }
    }

}
