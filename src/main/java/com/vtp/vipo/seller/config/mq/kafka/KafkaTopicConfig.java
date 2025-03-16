package com.vtp.vipo.seller.config.mq.kafka;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Configuration
public class KafkaTopicConfig {

    @Value("${custom.properties.kafka.topic.retries-event.name}")
    String retriesEventTopicName;

    @Value("${custom.properties.kafka.topic.vipo-seller-order-package-export-excel.name}")
    String orderPackageExportTopicName;

    @Value("${custom.properties.kafka.topic.vipo-seller-order-package-export-excel-retries.name}")
    String orderPackageExportRetryTopicName;

    @Value("${custom.properties.kafka.topic.vipo-seller-order-package-export-excel-dlq.name}")
    String orderPackageExportDLQTopicName;

    @Value("${custom.properties.kafka.topic.vipo-seller-revenue-export.name}")
    String revenueReportExportTopicName;

    @Value("${custom.properties.kafka.topic.vipo-seller-revenue-export-retries.name}")
    String revenueReportExportRetryTopicName;

    @Value("${custom.properties.kafka.topic.vipo-seller-revenue-export-dlq.name}")
    String revenueReportExportDLQTopicName;

    @Profile("local")
    @Bean
    public NewTopic createOrderPackageExportTopicLocal() {
        return new NewTopic(orderPackageExportTopicName, 12, (short) 2);
    }

    @Profile("dev")
    @Bean
    public NewTopic createOrderPackageExportTopicDev() {
        return new NewTopic(orderPackageExportTopicName, 12, (short) 2);
    }

    @Profile("prod")
    @Bean
    public NewTopic createOrderPackageExportTopicProd() {
        return new NewTopic(orderPackageExportTopicName, 12, (short) 2);
    }

    @Profile("local")
    @Bean
    public NewTopic createRetryTopicLocal() {
        return new NewTopic(retriesEventTopicName, 12, (short) 2);
    }

    @Profile("dev")
    @Bean
    public NewTopic createRetryTopicDev() {
        return new NewTopic(retriesEventTopicName, 12, (short) 2);
    }

    @Profile("prod")
    @Bean
    public NewTopic createRetryTopicProd() {
        return new NewTopic(retriesEventTopicName, 12, (short) 2);
    }


    @Profile("local")
    @Bean
    public NewTopic createOrderPackageExportRetryTopicLocal() {
        return new NewTopic(orderPackageExportRetryTopicName, 12, (short) 2);
    }

    @Profile("dev")
    @Bean
    public NewTopic createOrderPackageExportRetryTopicDev() {
        return new NewTopic(orderPackageExportRetryTopicName, 12, (short) 2);
    }

    @Profile("prod")
    @Bean
    public NewTopic createOrderPackageExportRetryTopicProd() {
        return new NewTopic(orderPackageExportRetryTopicName, 12, (short) 2);
    }

    @Profile("local")
    @Bean
    public NewTopic createOrderPackageExportDLQTopicLocal() {
        return new NewTopic(orderPackageExportDLQTopicName, 12, (short) 2);
    }

    @Profile("dev")
    @Bean
    public NewTopic createOrderPackageExportDLQTopicDev() {
        return new NewTopic(orderPackageExportDLQTopicName, 12, (short) 2);
    }

    @Profile("prod")
    @Bean
    public NewTopic createOrderPackageExportDLQTopicProd() {
        return new NewTopic(orderPackageExportDLQTopicName, 12, (short) 2);
    }

    @Bean
    public NewTopic createRevenueReportTopic() {
        return new NewTopic(revenueReportExportTopicName, 12, (short) 2);
    }

    @Bean
    public NewTopic createRevenueReportRetryTopic() {
        return new NewTopic(revenueReportExportRetryTopicName, 12, (short) 2);
    }

    @Bean
    public NewTopic createRevenueReportDLQTopic() {
        return new NewTopic(revenueReportExportDLQTopicName, 12, (short) 2);
    }


}
