package com.example.accounts_1.config;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.ClientRequestObservationConvention;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.boot.restclient.observation.ObservationRestClientCustomizer;

import java.util.concurrent.Executors;

@Configuration
public class HttpClientObservabilityConfig {

    // ObservationRestClientCustomizer is auto-configured by spring-boot-starter-opentelemetry.
    // Only define a ClientRequestObservationConvention bean if you want to customize tags/span names


    @Bean
    RestClientCustomizer tracingRestClientCustomizer(ObservationRegistry observationRegistry) {

        ClientRequestObservationConvention convention = new DefaultClientRequestObservationConvention();

        return new ObservationRestClientCustomizer(observationRegistry, convention);
    }


    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> contextPropagatingExecutorCustomizer() {

        ContextSnapshotFactory snapshotFactory = ContextSnapshotFactory.builder().build();

        return factory -> factory.configureExecutorService(
                ContextExecutorService.wrap(Executors.newCachedThreadPool(), snapshotFactory));
    }




}
