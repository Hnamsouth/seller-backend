package com.vtp.vipo.seller.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
public class AmazonS3Config {

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.endpoint-url}")
    private String endpointUrl;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.connection-timeout:5s}")
    private Duration connectionTimeout;

    @Value("${aws.s3.socket-timeout:25s}")
    private Duration socketTimeout;

    @Value("${aws.s3.proxy-host}")
    private String proxyHost;

    @Value("${aws.s3.proxy-port}")
    private Integer proxyPort;

    @Value("${aws.s3.isProd}")
    private boolean isProd;

//    @Profile({"dev", "local"})
//    @Bean(name = "amazonS3Client")
//    public AmazonS3 amazonS3ClientDev() {
//        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//
//        return AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, ""))
//                .withPathStyleAccessEnabled(true)
//                .build();
//    }
//
//    @Profile("prod")
    @Bean(name = "amazonS3Client")
    public AmazonS3 amazonS3ClientProd() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpointUrl, region);
        ClientConfiguration configClient = new ClientConfiguration();

        configClient.setConnectionTimeout((int) connectionTimeout.toMillis());
        configClient.setSocketTimeout((int) socketTimeout.toMillis());

        if (ObjectUtils.isNotEmpty(proxyHost) && ObjectUtils.isNotEmpty(proxyPort)) {
            configClient.setProxyHost(proxyHost);
            configClient.setProxyPort(proxyPort);
        }
        if (isProd) {
            configClient.setProtocol(Protocol.HTTPS);
            configClient.disableSocketProxy();
            configClient.isDisableHostPrefixInjection();
        }

        return AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(configClient)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(config)
                .build();
    }

}