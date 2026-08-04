package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ===================================================================================
 * @SpringBootApplication Annotation Explanation:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    Convenience annotation that bundles three core Spring annotations:
 *    - @Configuration: Marks the class as a source of bean definitions for the application context.
 *    - @EnableAutoConfiguration: Tells Spring Boot to automatically configure Spring features based on classpath jars.
 *    - @ComponentScan: Enables component scanning in the current package and all sub-packages (com.ecommerce.product.*).
 *
 * 2. WHY IT IS BETTER THAN ALTERNATIVES:
 *    Before Spring Boot, developers manually wrote hundreds of lines of XML or Java Config to wire up
 *    DataSources, Transaction Managers, DispatcherServlets, Jackson Mappers, etc.
 *
 * 3. WHEN IT SHOULD BE USED:
 *    Always on the main entry point class of a Spring Boot microservice.
 *
 * 4. COMMON MISTAKES DEVELOPERS MAKE:
 *    - Placing this class in a sub-package (e.g., com.ecommerce.product.config). Doing so causes component
 *      scanning to miss controllers, services, and repositories located outside that sub-package.
 * ===================================================================================
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
