# Temporal Contract Engine (TCE)

## Deterministic Event-Sourced Contract Execution Platform

Temporal Contract Engine (TCE) is a production-oriented backend platform for managing delayed, rule-bound, replayable commitments in distributed systems.

The system demonstrates advanced backend engineering concepts frequently encountered in financial infrastructure, workflow orchestration systems, settlement platforms, payment processing pipelines, compliance engines, and high-reliability distributed services.

Unlike traditional CRUD applications that overwrite state and lose historical context, TCE adopts an event-driven architecture that preserves every significant business action as an immutable event, enabling replayability, auditability, deterministic recovery, and operational transparency.

---

# Table of Contents

1. Executive Summary
2. Problem Statement
3. Business Motivation
4. Core Features
5. System Architecture
6. Component Overview
7. Technology Stack
8. Project Structure
9. Domain Model
10. Database Design
11. Contract Lifecycle
12. Event Sourcing
13. Replay Engine
14. Rule Engine
15. Idempotency
16. Optimistic Locking
17. Failure Recovery
18. Dead Letter Queue
19. Observability
20. API Reference
21. End-to-End Workflow
22. Testing Guide
23. Postman Validation Guide
24. Swagger Validation Guide
25. Verification Checklist
26. Engineering Tradeoffs
27. Resume Impact
28. Future Improvements

---

# Executive Summary

Temporal Contract Engine is a distributed commitment execution platform.

The system manages business commitments that:

* must execute later
* depend on rules
* require auditability
* require replayability
* require deterministic recovery

Examples include:

* delayed payouts
* subscription renewals
* settlement workflows
* compliance verification
* scheduled approvals
* rule-driven transactions

---

# Problem Statement

Most CRUD systems suffer from:

* duplicate processing
* race conditions
* overwritten state
* lack of audit trails
* difficult debugging
* poor failure recovery

Example:

User submits a payment request.

Network timeout occurs.

Client retries.

System creates duplicate records.

Result:

Financial loss.

Operational incidents.

Customer complaints.

Temporal Contract Engine solves these issues through:

* Idempotent APIs
* Event Sourcing
* Replay Engine
* Distributed Scheduling
* Optimistic Locking
* Failure Recovery

---

# Core Features

### Contract Lifecycle Management

```text
PENDING
ELIGIBLE
EXECUTING
COMPLETED
FAILED
CANCELLED
```

### Event Sourcing

Every business action becomes an immutable event.

### Replay Engine

State can be reconstructed from event history.

### Rule DSL

Business rules are configurable.

### Kafka Event Streaming

Events are published for asynchronous processing.

### Redis Coordination

Used for idempotency and distributed coordination.

### Retry Mechanism

Automatic recovery from transient failures.

### Dead Letter Queue

Failed executions are isolated for investigation.

### Prometheus Metrics

Operational visibility.

---

# System Architecture

```text
Client
 │
 ▼
REST API
 │
 ▼
Controllers
 │
 ▼
Services
 │
 ├──────────────► PostgreSQL
 │
 ├──────────────► Redis
 │
 ├──────────────► Event Store
 │
 ├──────────────► Scheduler
 │
 └──────────────► Kafka/Redpanda
                        │
                        ▼
                 Event Consumers
```

# Temporal Contract Engine

## Architecture

![System Architecture](images/system_architecture.png)

## Dashboard

![Dashboard](images/dashboard.png)

## Rules Engine

![Rules](images/rules.png)

## Execution Logs

![Execution Logs](images/execution_logs.png)

## Metrics Center

![Metrics Center](images/metrics_center.png)

## System Health

![System Health](images/system_health.png)

## Contract Status

### Successful Contract
![Successful Contract](images/succesful_contract.png)

### Failed Contract
![Failed Contract](images/failed_contract.png)

## Dead Letter Queue

![Dead Letter Queue](images/dead_letter_queue.png)

## Replay Audit

![Replay Audit](images/replay_audit.png)

## About

![About](images/about.png)

---

# Technology Stack

| Layer      | Technology       |
| ---------- | ---------------- |
| Language   | Java 21          |
| Framework  | Spring Boot 3    |
| Database   | PostgreSQL       |
| Cache      | Redis            |
| Messaging  | Kafka / Redpanda |
| Monitoring | Prometheus       |
| Metrics    | Micrometer       |
| API Docs   | Swagger/OpenAPI  |
| Build Tool | Maven            |
| Containers | Docker           |

---





# Domain Model

## Contract

Represents a delayed commitment.

Fields:

* id
* name
* description
* status
* effectiveDate
* expirationDate
* creationDate
* lastModifiedDate

---

## Rule

Represents a business constraint.

Fields:

* id
* contractId
* conditionExpression
* actionPayload
* priority

---

## EventStore

Immutable audit log.

Fields:

* eventId
* aggregateId
* aggregateType
* eventType
* payload
* occurredAt

---

## ExecutionLog

Execution history.

Fields:

* id
* contractId
* executionTimestamp
* status
* details

---

## DeadLetterExecution

Stores permanently failed executions.

Fields:

* id
* contractId
* failedAt
* errorMessage
* stackTrace

---

# API Reference:8080

---

## Health APIs

### GET /health

Purpose:

Basic health verification.

Response

```json
{
  "status": "UP"
}
```

---

### GET /actuator/health

Checks:

* Database
* Redis
* Kafka
* Spring Boot

---

### GET /actuator/prometheus

Returns metrics.

Important Metrics:

```text
contract_creation_count_total
execution_success_count_total
execution_failure_count_total
scheduler_backlog
```

---

# Contract APIs

## GET /api/v1/contracts

Purpose:

List all contracts.

Response:

```json
[
  {
    "id":"uuid",
    "name":"Contract",
    "status":"PENDING"
  }
]
```

---

## POST /api/v1/contracts

Purpose:

Create contract.

Request

```json
{
  "name":"Premium Subscription Renewal",
  "description":"Monthly renewal workflow",
  "effectiveDate":"2030-01-01T00:00:00Z",
  "expirationDate":"2031-01-01T00:00:00Z",
  "ruleIds":[]
}
```

Headers

```http
Content-Type: application/json
Idempotency-Key: unique-key-123
```

Success Response

```json
{
  "id":"contract-uuid",
  "status":"PENDING"
}
```

Generated Event

```text
CONTRACT_CREATED
```

---

## GET /api/v1/contracts/{id}

Purpose:

Retrieve contract.

Response

```json
{
  "id":"contract-uuid",
  "status":"PENDING"
}
```

---

## POST /api/v1/contracts/{id}/execute

Purpose:

Execute contract.

Request

```json
{
  "balance":1500,
  "risk_score":20
}
```

Possible Events

```text
CONTRACT_EXECUTED
EXECUTION_FAILED
```

---

## POST /api/v1/contracts/{id}/cancel

Purpose:

Cancel contract.

Generated Event

```text
CONTRACT_CANCELLED
```

---

## GET /api/v1/contracts/{id}/events

Purpose:

Retrieve event history.

Response

```json
[
  {
    "eventType":"CONTRACT_CREATED"
  },
  {
    "eventType":"CONTRACT_EXECUTED"
  }
]
```

---

## GET /api/v1/contracts/{id}/execution-logs

Purpose:

Retrieve execution history.

Response

```json
[
  {
    "status":"SUCCESS",
    "details":"Execution completed"
  }
]
```

---

# Rule APIs

## POST /api/v1/rules

Request

```json
{
  "contractId":"contract-uuid",
  "name":"Balance Validation",
  "description":"Approve only high balance accounts",
  "conditionExpression":"balance > 1000",
  "actionPayload":"{\"approved\":true}",
  "priority":1
}
```

Generated Event

```text
RULE_CREATED
```

---

## GET /api/v1/rules/{id}

Retrieve rule.

---

# Admin APIs

## POST /admin/replay/{contractId}

Purpose:

Replay contract state.

Response

```json
{
  "contractId":"contract-id",
  "consistent":true,
  "discrepancies":[]
}
```

---

## GET /admin/dead-letters

Purpose:

View permanently failed executions.

Response

```json
[
  {
    "contractId":"uuid",
    "errorMessage":"Failure details"
  }
]
```

---

# End-to-End Validation Workflow

## Step 1

Verify health

GET

```text
/actuator/health
```

Expected

```json
{
  "status":"UP"
}
```

---

## Step 2

Create Contract

POST

```text
/api/v1/contracts
```

Save:

```text
CONTRACT_ID
```

---

## Step 3

Verify Contract

GET

```text
/api/v1/contracts/{CONTRACT_ID}
```

---

## Step 4

Create Rule

POST

```text
/api/v1/rules
```

Save:

```text
RULE_ID
```

---

## Step 5

Verify Rule

GET

```text
/api/v1/rules/{RULE_ID}
```

---

## Step 6

Execute Contract

POST

```text
/api/v1/contracts/{CONTRACT_ID}/execute
```

---

## Step 7

Verify Event Store

GET

```text
/api/v1/contracts/{CONTRACT_ID}/events
```

Expected

```text
CONTRACT_CREATED
RULE_CREATED
CONTRACT_EXECUTED
```

---

## Step 8

Verify Execution Logs

GET

```text
/api/v1/contracts/{CONTRACT_ID}/execution-logs
```

---

## Step 9

Replay State

POST

```text
/admin/replay/{CONTRACT_ID}
```

Expected

```json
{
  "consistent": true
}
```

---

## Step 10

Verify Metrics

GET

```text
/actuator/prometheus
```

Verify:

```text
contract_creation_count_total
execution_success_count_total
execution_failure_count_total
```

---

# Engineering Concepts Demonstrated

✓ Event Sourcing

✓ Replay Engine

✓ Optimistic Locking

✓ Idempotency

✓ Retry Backoff

✓ Dead Letter Queues

✓ Distributed Scheduling

✓ Kafka Messaging

✓ Redis Coordination

✓ State Machines

✓ Auditability

✓ Operational Observability

✓ Failure Recovery

✓ Production-Oriented Backend Architecture



---

# License

Apache License 2.0
