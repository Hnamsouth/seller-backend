package com.vtp.vipo.seller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class VipoSellerApplication {

	/** The name of the application, set during initialization. */
	public static final String APPLICATION_NAME = "vipo-seller";
	public static final String APPLICATION_VERSION = "1.0.2";

	public static void main(String[] args) {
		SpringApplication.run(VipoSellerApplication.class, args);
	}

}
