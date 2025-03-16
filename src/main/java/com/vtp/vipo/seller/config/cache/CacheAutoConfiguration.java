package com.vtp.vipo.seller.config.cache;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration class enables the use of CacheProperties for the application. It uses
 * the @Configuration annotation to indicate that it is a configuration class. The proxyBeanMethods
 * attribute is set to false to indicate that CGLIB proxying should not be used. Instead, a simple
 * instance of the class is created, and bean methods are invoked directly on that instance.
 * The @EnableConfigurationProperties annotation is used to enable support
 * for @ConfigurationProperties annotated beans. In this case, it enables the CacheProperties class,
 * which means that properties defined in that class can be bound to the environment. This class
 * doesn't define any beans, it only provides configuration.
 *
 * @author haidv
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {
}
