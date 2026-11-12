package com.example.gatewayserver;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GatewayserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserverApplication.class, args);
	}

	//CUStom routing
	@Bean
	public RouteLocator eazyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) { 
		return routeLocatorBuilder.routes()

                //circuitBreaker
				.route(p -> p.path("/jbmicroservice/accounts-1/**")
						.filters(f -> f.rewritePath("/jbmicroservice/accounts-1/(?<segment>.*)", "/${segment}")
									   .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
						 .circuitBreaker(config -> config.setName("accountsCircuitBreaker")   // comment this when implement CB in individual MS level
							   .setFallbackUri("forward:/contactSupport")
						)
								)
						.uri("lb://accounts-1")
				)

                //retry
				.route(p -> p.path("/jbmicroservice/loans-1/**")
						.filters(f -> f.rewritePath("/jbmicroservice/loans-1/(?<segment>.*)", "/${segment}")
										.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
										.retry(retry -> retry.setRetries(3)                                     		// 3 times retry
												.setMethods(org.springframework.http.HttpMethod.GET)      				//retry for Get is safe, for post its risk
												.setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)  	//wait 100ms for 1st retry, 1000ms between 2 retry , factor 2 
										)
						 )
						.uri("lb://loans-1")
				)
                //RateLimiter
                .route(p -> p.path("/jbmicroservice/cards-1/**")
                        .filters(f -> f.rewritePath("/jbmicroservice/cards-1/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("lb://cards-1"))
                .build();


       /*         RouteLocatorBuilder.Builder routesBuilder = builder.routes();
                PredicateSpec predicateSpec = routesBuilder.route("loans_route_id");
                var gatewayFilterSpec = predicateSpec.path("/jbmicroservice/loans-1/**");

                gatewayFilterSpec.rewritePath("/jbmicroservice/loans-1/(?<segment>.*)", "/${segment}");
                gatewayFilterSpec.addResponseHeader("X-Response-Time", LocalDateTime.now().toString());

                RetryGatewayFilterFactory.RetryConfig retryConfig = new RetryGatewayFilterFactory.RetryConfig();
                retryConfig.setRetries(3);
                retryConfig.setMethods(Collections.singletonList(HttpMethod.GET));

                RetryGatewayFilterFactory.BackoffConfig backoffConfig = new RetryGatewayFilterFactory.BackoffConfig();
                backoffConfig.setFirstBackoff(Duration.ofMillis(100));
                backoffConfig.setMaxBackoff(Duration.ofMillis(1000));
                backoffConfig.setFactor(2);
                backoffConfig.setBasedOnPreviousValue(true);
                retryConfig.setBackoff(backoffConfig);

                gatewayFilterSpec.retry(retryConfig);
                Route.AsyncBuilder route = gatewayFilterSpec.uri("lb://loans-1");*/
	}
	
	//To configure/change default CB retry/timeout
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() { //ReactiveResilience4JCircuitBreakerFactory
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
													.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
													.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
				.build());
	}

	//Rate limiter
	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 1, 1);
		/*defaultReplenishRate --how many tokens per second in token-bucket algorithm.
		defaultBurstCapacity --how many tokens the bucket can hold in token-bucketalgorithm.
		defaultRequestedTokens --how many tokens are requested per request.*/
	}
	
	@Bean
	KeyResolver userKeyResolver() {
		return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
				.defaultIfEmpty("anonymous");
	}
	

}
