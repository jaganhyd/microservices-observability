# Cloud-Native Banking Microservices — Resiliency & Observability

A fault-tolerant, observable digital banking microservices platform built with **Java 17** and **Spring Boot 3**. This repository demonstrates centralized configuration management, service discovery, API gateway routing, Resilience4j fault-tolerance patterns, and full-stack observability (distributed tracing, metrics, and log aggregation) across three core banking domain services: **Accounts**, **Cards**, and **Loans**.

## Architecture Overview

```text
                        +------------------------+

                        |   Spring Cloud Config  |
                        |     (Port: 8071)       |
                        +-----------+------------+
                                    |
+-----------+           +-----------v------------+           +--------------------+

|  Client   | --------> |  Spring Cloud Gateway  | --------> |  Netflix Eureka    |
| (Postman) |           |     (Port: 8072)       |           |   (Port: 8070)     |
+-----------+           +---+-------+--------+---+           +--------------------+

                            |       |        |
            +---------------+       |        +---------------+
            v                       v                        v
+-------------------+   +-------------------+    +-------------------+

| Accounts Service  |   |   Cards Service   |    |   Loans Service   |
|  (Port: 8080)     |   |  (Port: 9000)     |    |  (Port: 8090)     |
+---------+---------+   +---------+---------+    +---------+---------+

          |                       |                        |
          +-----------------------+------------------------+
                                  |
              +-------------------+-------------------+
              v                   v                   v
     +-----------------+ +-----------------+ +-----------------+

     |   Prometheus    | |     Grafana     | |   Zipkin / Tempo|
     |   (Metrics)     | |  (Dashboards)   | |   (Tracing)     |
     +-----------------+ +-----------------+ +-----------------+
```

## Core Microservices & Infrastructure Components

* **Accounts Service (`accounts`):** Manages customer account profiles, account creation, and aggregated customer portfolio views (invoking Cards and Loans via OpenFeign).
* **Cards Service (`cards`):** Handles debit/credit card issuance, card limits, and usage tracking.
* **Loans Service (`loans`):** Manages home, personal, and vehicle loan records and repayment schedules.
* **Config Server (`configserver`):** Centralized externalized configuration backed by Git/filesystem profiles.
* **Service Discovery (`eurekaserver`):** Dynamic service registration and client-side load balancing using Netflix Eureka.
* **API Gateway (`gatewayserver`):** Single entry point handling dynamic routing, cross-cutting filters, and request correlation IDs.

## Key Architectural Patterns Implemented

* **Fault Tolerance & Resiliency (Resilience4j):**
  * **Circuit Breaker:** Prevents cascading failures when downstream Cards or Loans services experience latency or downtime.
  * **Retry:** Automatically retries transient network failures with configurable backoff intervals.
  * **Rate Limiter:** Protects API Gateway endpoints from traffic spikes and abusive request rates.
  * **Fallback Mechanisms:** Returns graceful, cached/default responses when circuit breakers open.
* **Observability & Distributed Tracing:**
  * **Micrometer & OpenTelemetry / Zipkin:** End-to-end trace propagation (`traceId` and `spanId`) across Gateway and downstream microservices.
  * **Prometheus & Grafana:** Real-time JVM, HTTP latency, and Resilience4j circuit-breaker state metrics visualization.

## Tech Stack

* **Language & Framework:** Java 25, Spring Boot 4, Spring Cloud (Config, Gateway, Netflix Eureka, OpenFeign)
* **Resiliency & Monitoring:** Resilience4j, Spring Boot Actuator, Micrometer, Zipkin, Prometheus, Grafana
* **Containerization:** Docker, Docker Compose

## Quick Start (Docker Compose)

1. **Clone the repository:**
   ```bash
   git clone https://github.com
   cd microservices-observability
   ```

2. **Build all microservices and Docker images:**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start the entire ecosystem:**
   ```bash
   docker-compose up -d
   ```

4. **Verify Service Health & Dashboards:**
   * **Eureka Dashboard:** `http://localhost:8070`
   * **API Gateway Base URL:** `http://localhost:8072`
   * **Zipkin Tracing UI:** `http://localhost:9411`
   * **Prometheus Metrics:** `http://localhost:9090`
   * **Grafana Dashboards:** `http://localhost:3000`
