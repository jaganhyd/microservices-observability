package com.example.accounts_1.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryLoggingConfig {

    @Bean
    ApplicationListener<ApplicationReadyEvent> openTelemetryAppenderInstaller(OpenTelemetry openTelemetry) {

        return event -> OpenTelemetryAppender.install(openTelemetry);


        /*return new ApplicationListener<ApplicationReadyEvent>() {
            @Override
            public void onApplicationEvent(ApplicationReadyEvent event) {
                OpenTelemetryAppender.install(openTelemetry);
            }
        };*/
    }
}

/*
we declared the appender in xml file .
The appender name is OpenTelemetry,
but that appender is just sitting there.
It does not yet have a connection
to the actual OpenTelemetry SDK
that Spring Boot configured for us.
It's like installing a phone,
but never plugging it into the line.
 So this Bean makes the connection.

we are injecting OpenTelemetry instance
that we receive from the framework.
We are trying to link up these OpenTelemetry SDK
to our appender.*/
