package com.example.gatewayserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

	@RequestMapping("/contactSupport")
	public Mono<String> contactSupport() {

        //logger.warn("Circuit breaker fallback triggered, routing request to contactSupport");
        logger.error("An error occured, please try after sometime or contact support team");
		return Mono.just("An error occured, please try after sometime or contact support team");
	}
}
