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

    //Its job is to force tracing instrumentation
    //onto our outbound calls that we are trying to make
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

   /* Our account service protects its calls
    with a circuit breaker,
    and we are using the blocking circuit breaker
    to enforce its timeout.
    That circuit breaker does not run the protected call
    on our current thread.
    It hands it off to run on a separate worker thread
    from its own thread pool,
    and you already know what that means.
    That worker thread starts completely fresh
    with no trace context.
    The baton got dropped the instant the circuit breaker
    switched to threads, so even if bean one
    is ready to stamp the label, there is nothing to stamp.
    The context is not there on the worker thread,
    so the bean two wraps that circuit breaker's thread pool
    with the context propagation,*/




}
