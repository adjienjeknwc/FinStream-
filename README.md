<div align="center">

# 🏦 FinStream
### Enterprise Banking & Loan Platform

**A resilient, event-driven core banking microservices backend and administrative control panel — demonstrating production-grade enterprise integration patterns, workflow orchestration, and fault-tolerant telemetry.**

[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Camunda](https://img.shields.io/badge/Camunda_8_(Zeebe)-FC5D0D?style=for-the-badge&logo=camunda&logoColor=white)](https://camunda.com/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-FF6B6B?style=for-the-badge&logo=java&logoColor=white)](https://resilience4j.readme.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)](https://www.elastic.co/)
[![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS_v4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](CONTRIBUTING.md)
[![Status](https://img.shields.io/badge/Status-Reference%20Implementation-blue?style=flat-square)]()

</div>

---

## 📖 Overview

**FinStream** is a reference implementation of a resilient, event-driven **core banking microservices backend** paired with an administrative **compliance control tower**. It's built to demonstrate production-ready patterns for enterprise financial systems: **enterprise integration patterns (EIP)**, **workflow orchestration via Camunda 8**, **circuit-breaker fault tolerance**, and **Kafka-based dead-letter recovery** — all observable in real time through a live dashboard.

Rather than just describing these patterns, FinStream ships with **two interactive resiliency simulations** (circuit breaker trip + DLQ recovery) so you can watch the system degrade gracefully and self-heal, end to end.

> 📸 *Add screenshots or a GIF here — the compliance dashboard with the Circuit Breaker telemetry badge (CLOSED/OPEN) and the DLQ recovery flow are great visuals to lead with.*

---

## 🏗️ System Architecture

```
                                   ┌─────────────────────────┐
                                   │   Compliance Control       │
                                   │   Tower (React + Vite)      │
                                   │   :5173                     │
                                   └──────────────┬──────────────┘
                                                  │ REST
                                                  ▼
                                   ┌─────────────────────────┐
                                   │      API Gateway            │
                                   │         :8080                │
                                   └──────────────┬──────────────┘
                        ┌───────────────────────┼───────────────────────┐
                        ▼                        ▼                       ▼
          ┌───────────────────────┐  ┌───────────────────────┐  ┌───────────────────────┐
          │ Account Management       │  │  Loan Workflow Service    │  │ Notification & Audit     │
          │ Service · :8083           │  │  Service · :8081           │  │ Service · :8082           │
          │ (Balances, KYC)            │  │ (Camunda 8 / Zeebe,         │  │ (Kafka consumer,           │
          │ PostgreSQL                  │  │  Resilience4j Circuit       │  │  DLQ routing)               │
          │                              │  │  Breaker, Credit Bureau)    │  │                              │
          └───────────────────────┘  └───────────┬───────────┘  └──────────────┬────────────┘
                                                    │ publishes events               │ consumes
                                                    ▼                                │
                                       ┌─────────────────────────┐                  │
                                       │      Apache Kafka           │◀─────────────┘
                                       │   (+ Zookeeper coord.)       │
                                       └──────────────┬──────────────┘
                                                      ▼
                                       ┌─────────────────────────┐
                                       │   loan-audit-stream.DLT    │
                                       │   (Dead Letter Queue)       │
                                       └─────────────────────────┘
```

---

## 🧩 Microservices Directory

<div align="center">

| Service | Port | Responsibility |
|---|---|---|
| 🚪 **`api-gateway`** | `8080` | Routes external requests to backing modules |
| 👤 **`account-management-service`** | `8083` | Manages account balances & KYC status (PostgreSQL) |
| 💳 **`loan-workflow-service`** | `8081` | Embeds Camunda 8 (Zeebe); executes Credit Bureau calls behind Resilience4j Circuit Breakers; publishes transaction events to Kafka |
| 🔔 **`notification-audit-service`** | `8082` | Consumes transactional streams from Kafka; routes exceptions to a Dead Letter Queue (DLQ) |
| 🛡️ **`compliance-control-tower`** | `5173` | Vite + React + TypeScript + Tailwind CSS v4 administrative dashboard |

</div>

---

## 🛠️ Tech Stack

<div align="center">

| Layer | Technologies |
|---|---|
| **Backend** | Java 21 · Spring Boot 3.3.x (Spring Web, Spring Data JPA, Spring AOP) · Camunda 8 (Zeebe) · Spring Kafka · Resilience4j · Maven |
| **Frontend** | React.js · TypeScript · Tailwind CSS v4 · Vite · Lucide React |
| **Databases & Infra** | PostgreSQL 16 · Apache Kafka · Elasticsearch · Zookeeper · Docker Compose |

</div>

---

## 🚀 Run Guide

### Prerequisites

- Java JDK 21
- Maven 3.9+
- Node.js 18+ & npm
- Docker Desktop

### Step 1 — Spin Up Infrastructure

Stand up PostgreSQL, Kafka brokers, and Zeebe containers from the project root:

```bash
docker compose up -d
```

### Step 2 — Compile & Run Backend Services

Build all submodules from the parent directory:

```bash
# Compile and run unit tests
mvn clean test

# Package JARs
mvn clean package -DskipTests
```

Then run each Spring Boot microservice in its own terminal:

```bash
# Terminal 1: Account Service
cd account-management-service && mvn spring-boot:run

# Terminal 2: Loan Service
cd loan-workflow-service && mvn spring-boot:run

# Terminal 3: Audit Service
cd notification-audit-service && mvn spring-boot:run

# Terminal 4: API Gateway Router
cd api-gateway && mvn spring-boot:run
```

### Step 3 — Run the Dashboard UI

```bash
cd compliance-control-tower
npm install
npm run dev
```

Navigate to **`http://localhost:5173`** (or `5174` if `5173` is occupied).

---

## 🧪 Resiliency Scenarios to Test

### 🔴 Scenario A — Resilience4j Circuit Breaker Trip

| Step | Action |
|---|---|
| 1 | Open the dashboard — the Circuit Breaker telemetry indicator reads `CLOSED` (healthy) |
| 2 | Inject a new transaction — the system queries the external Bureau API and completes successfully |
| 3 | Toggle **"Simulate Circuit Failover"** to degrade downstream service health |
| 4 | The Circuit Breaker badge trips to `OPEN — FAILOVER ACTIVE` |
| 5 | Submit a new application — the service intercepts the call and runs the fallback scorer, safely defaulting the applicant's rating to `600` to prevent risk leakage |

### 🟡 Scenario B — Kafka Dead Letter Queue (DLQ) Recovery

| Step | Action |
|---|---|
| 1 | In the simulator panel, set the customer Account ID to `"poison-pill"` |
| 2 | Submit the transaction |
| 3 | The serializer aspect intercepts the value and throws a parsing exception |
| 4 | Instead of blocking processing threads, `notification-audit-service` isolates the payload and routes it to the `loan-audit-stream.DLT` topic, safely committing the offset |

> These two scenarios demonstrate **graceful degradation** (fail-safe defaults over hard failure) and **fault isolation** (poison messages don't block the pipeline) — two of the core resiliency guarantees the platform is built around.

---

## 🧠 Engineering Patterns Demonstrated

- **Circuit Breaker Pattern** (Resilience4j) — prevents cascading failure when the external Credit Bureau API degrades
- **Dead Letter Queue Pattern** (Kafka) — isolates malformed/poison messages without halting stream processing
- **Workflow Orchestration** (Camunda 8 / Zeebe) — models the loan approval process as an explicit, observable BPMN workflow
- **Event-Driven Architecture** (Kafka) — decouples transaction publishing (loan service) from consumption (audit service)
- **API Gateway Pattern** — single entry point routing to independently deployable microservices
- **Aspect-Oriented Programming** (Spring AOP) — cross-cutting concerns like serialization interception

---

## 🗺️ Roadmap

- [ ] Distributed tracing across services (OpenTelemetry)
- [ ] Centralized log aggregation via Elasticsearch + Kibana
- [ ] Authentication/authorization layer for the API Gateway (OAuth2 / Keycloak)
- [ ] Kubernetes deployment manifests as an alternative to Docker Compose
- [ ] Automated chaos-testing suite for additional resiliency scenarios

---

## ⚠️ Disclaimer

FinStream is a **reference / educational implementation** built to demonstrate enterprise banking architecture patterns. It is not a production financial system and should not be used to process real financial transactions or real customer data.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](../../issues).

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with 🏦 and a genuine appreciation for graceful degradation.**

</div>
