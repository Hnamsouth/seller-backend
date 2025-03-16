package com.vtp.vipo.seller.config.mq.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaderMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * KafkaConfig is a Spring configuration class that sets up Kafka producer and consumer settings.
 * It configures thread pools, consumer factories, listener container factories, header mappers,
 * producer factories, and Kafka templates. This setup facilitates efficient and reliable
 * communication between the application and Kafka brokers.
 *
 * <p>
 * Features:
 * <ul>
 *     <li>Custom thread pool for Kafka consumers to handle concurrent message processing.</li>
 *     <li>Consumer factory with specific configurations for deserialization, polling behavior, and isolation levels.</li>
 *     <li>Listener container factory supporting batch listeners with manual acknowledgment.</li>
 *     <li>Header mapper to map Kafka message headers to Spring's message headers.</li>
 *     <li>Producer factory with serializer configurations and idempotence settings.</li>
 *     <li>Kafka template for high-level message publishing.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Ensure that the following properties are defined in your application properties or YAML file:
 * <ul>
 *     <li><code>custom.properties.kafka.bootstrap-servers</code></li>
 *     <li><code>custom.properties.messaging.kafka.consumer.batch</code></li>
 *     <li><code>custom.properties.kafka.consumer.max.timeout</code></li>
 *     <li><code>custom.properties.messaging.kafka.consumer.number.of.message.in.batch</code></li>
 *     <li><code>custom.properties.messaging.consumer.pool.size</code></li>
 *     <li><code>custom.properties.graceful.shutdown.messaging.consumer.wait.time.max</code></li>
 *     <li><code>custom.properties.messaging.consumer.pool.thread.name.prefix</code></li>
 * </ul>
 * </p>
 *
 * <p>
 * Example configuration in <code>application.yml</code>:
 * </p>
 *
 * <pre>{@code
 * custom:
 *   properties:
 *     kafka:
 *       bootstrap-servers: localhost:9092
 *       consumer:
 *         max:
 *           timeout: 300000
 *     messaging:
 *       kafka:
 *         consumer:
 *           batch: true
 *           number:
 *             of:
 *               message:
 *                 in:
 *                   batch: 50
 *       consumer:
 *         pool:
 *           size: 10
 *           thread:
 *             name:
 *               prefix: kafka-consumer-
 *       graceful:
 *         shutdown:
 *           messaging:
 *             consumer:
 *               wait:
 *                 time:
 *                   max: 60
 * }</pre>
 *
 * @author
 * @version 1.0
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    // Kafka broker addresses
    @Value("${custom.properties.kafka.bootstrap-servers}")
    private String bootstrapServerUrl;

    // Flag to enable batch consumption
    @Value("${custom.properties.messaging.kafka.consumer.batch}")
    private boolean isBatchConsumerNapasTransfer;

    // Maximum timeout for consumer polling (in milliseconds)
    @Value("${custom.properties.kafka.consumer.max.timeout}")
    private int consumerTimeout;

    // Maximum number of records per batch for batch consumers
    @Value("${custom.properties.messaging.kafka.consumer.number.of.message.in.batch}")
    private int maxBatchRecordNapasTransfer;

    // Number of threads in the consumer thread pool
    @Value("${custom.properties.messaging.consumer.pool.size}")
    private int kafkaConsumerThreadPoolSize;

    // Maximum wait time during graceful shutdown (in seconds)
    @Value("${custom.properties.graceful.shutdown.messaging.consumer.wait.time.max}")
    private int waitTimeMax;

    // Prefix for consumer thread names (e.g., "kafka-consumer-")
    @Value("${custom.properties.messaging.consumer.pool.thread.name.prefix}")
    private String threadNamePrefix;

    /**
     * Configures a thread pool executor for Kafka consumers. This executor manages the threads
     * that process incoming Kafka messages, enabling concurrent processing and efficient resource
     * utilization.
     *
     * <p>
     * The thread pool is configured with the following settings:
     * <ul>
     *     <li><strong>Core Pool Size:</strong> Number of threads that are always kept running.</li>
     *     <li><strong>Max Pool Size:</strong> Maximum number of threads allowed in the pool.</li>
     *     <li><strong>Allow Core Thread Timeout:</strong> Allows core threads to terminate if idle.</li>
     *     <li><strong>Wait for Tasks to Complete on Shutdown:</strong> Ensures ongoing tasks finish before shutdown.</li>
     *     <li><strong>Await Termination Seconds:</strong> Maximum time to wait for tasks to complete during shutdown.</li>
     *     <li><strong>Thread Name Prefix:</strong> Prefix for naming threads for easier identification.</li>
     * </ul>
     * </p>
     *
     * @return Configured ThreadPoolTaskExecutor for Kafka consumers.
     */
    @Bean(name = "kafkaConsumerThreadPool")
    public ThreadPoolTaskExecutor kafkaConsumerThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(kafkaConsumerThreadPoolSize);
        executor.setMaxPoolSize(kafkaConsumerThreadPoolSize);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(waitTimeMax);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    /**
     * Configures the Kafka ConsumerFactory with specific consumer properties. The ConsumerFactory
     * is responsible for creating Kafka consumer instances with the defined settings.
     *
     * <p>
     * Key configurations include:
     * <ul>
     *     <li><strong>Bootstrap Servers:</strong> Addresses of Kafka brokers.</li>
     *     <li><strong>Max Poll Records:</strong> Maximum number of records returned in a single poll.</li>
     *     <li><strong>Enable Auto Commit:</strong> Disables automatic offset commits for manual control.</li>
     *     <li><strong>Isolation Level:</strong> Ensures consumers read only committed messages.</li>
     *     <li><strong>Max Poll Interval:</strong> Maximum delay between poll invocations.</li>
     *     <li><strong>Auto Offset Reset:</strong> Starts reading from the earliest offset if no previous offset is found.</li>
     *     <li><strong>Deserializers:</strong> Configures how message keys and values are deserialized.</li>
     * </ul>
     * </p>
     *
     * @return Configured ConsumerFactory for Kafka consumers.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxBatchRecordNapasTransfer);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, consumerTimeout);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Configures the Kafka Listener Container Factory, which creates listener containers for
     * processing incoming Kafka messages. This factory integrates the consumer factory, thread
     * pool executor, and acknowledgment mode to manage message consumption efficiently.
     *
     * <p>
     * Key configurations include:
     * <ul>
     *     <li><strong>Consumer Factory:</strong> Associates the configured ConsumerFactory with the listener.</li>
     *     <li><strong>Batch Listener:</strong> Enables or disables batch processing of messages.</li>
     *     <li><strong>Acknowledgment Mode:</strong> Sets manual immediate acknowledgment for precise offset control.</li>
     *     <li><strong>Listener Task Executor:</strong> Assigns the custom thread pool executor for handling listener tasks.</li>
     * </ul>
     * </p>
     *
     * @return Configured ConcurrentKafkaListenerContainerFactory for Kafka consumers.
     */
    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // Associate the consumer factory
        factory.setConsumerFactory(consumerFactory());

        // Enables batch listening if set to true, allowing consumers to receive a batch of messages at once.
        factory.setBatchListener(isBatchConsumerNapasTransfer);

        // Set acknowledgment mode to manual immediate for manual offset management
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Assign the custom thread pool executor for listener tasks
        factory.getContainerProperties().setListenerTaskExecutor(kafkaConsumerThreadPool());

        return factory;
    }

    /**
     * Configures the Kafka Header Mapper, which maps Kafka message headers to Spring's
     * MessageHeaders and vice versa. This mapper ensures that metadata associated with
     * Kafka messages is preserved and accessible within the application.
     *
     * <p>
     * Key configurations include:
     * <ul>
     *     <li><strong>Map All String Headers Out:</strong> Ensures that all string headers are mapped.</li>
     * </ul>
     * </p>
     *
     * @return Configured KafkaHeaderMapper for mapping message headers.
     */
    @Bean(name = "kafkaBinderHeaderMapper")
    public KafkaHeaderMapper kafkaBinderHeaderMapper() {
        DefaultKafkaHeaderMapper mapper = new DefaultKafkaHeaderMapper();
        mapper.setMapAllStringsOut(true);
        return mapper;
    }

    /**
     * Configures the Kafka ProducerFactory with specific producer properties. The ProducerFactory
     * is responsible for creating Kafka producer instances with the defined settings.
     *
     * <p>
     * Key configurations include:
     * <ul>
     *     <li><strong>Bootstrap Servers:</strong> Addresses of Kafka brokers.</li>
     *     <li><strong>Serializers:</strong> Configures how message keys and values are serialized.</li>
     *     <li><strong>Enable Idempotence:</strong> Controls idempotent message sending to prevent duplicates.</li>
     * </ul>
     * </p>
     *
     * @return Configured ProducerFactory for Kafka producers.
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka broker addresses
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);

        // Serializers for key and value
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Disable idempotence to allow possible duplicate messages in failure scenarios
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Configures the KafkaTemplate, which provides a high-level abstraction for sending messages
     * to Kafka topics. It leverages the ProducerFactory to create producer instances and simplifies
     * message publishing with convenient methods.
     *
     * @return Configured KafkaTemplate for sending messages.
     */
    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
