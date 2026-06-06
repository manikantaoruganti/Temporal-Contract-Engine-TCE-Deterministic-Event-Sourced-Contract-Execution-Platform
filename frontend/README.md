# Temporal Contract Engine - Control Center

This is the world-class operational control center for the Temporal Contract Engine. It visually demonstrates event sourcing, replayability, state reconstruction, and distributed systems reliability.

## 🚀 Getting Started

The easiest way to run the entire stack (Frontend + Backend + Database + Kafka + Redis + Prometheus) is via Docker Compose from the root directory:

```bash
cd ..
docker compose up --build -d
```

Once running, access the services:
- **Frontend Dashboard:** [http://localhost:3000](http://localhost:3000)
- **Backend API:** [http://localhost:8080](http://localhost:8080)
- **Swagger Documentation:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 🛠️ Technology Stack

- **React 19 + Vite:** Fast, modern frontend tooling
- **Material UI (MUI):** Enterprise-grade component library
- **React Query + Axios:** Server state management and API client
- **Framer Motion:** Micro-animations and page transitions
- **Recharts:** Data visualization for the operational dashboard
- **React Flow:** Interactive architecture diagrams
- **Monaco Editor:** VS Code-powered JSON payload inspector

## 🌟 Key Features

1. **Replay Engine (Hero Feature):** Cryptographically verify that the aggregate state in the primary database exactly matches a state rebuilt entirely from the immutable event log (`Replay(Events) === CurrentState`).
2. **Event Explorer:** Inspect the granular, time-sequenced domain events that drive the state machine.
3. **State Machine Visualization:** Track contracts as they flow through deterministic lifecycle phases (`PENDING` → `EXECUTING` → `COMPLETED`/`FAILED`).
4. **Dead Letter Recovery:** Isolate and inspect failed executions with preserved stack traces for operational recovery.
5. **System Health & Metrics:** Real-time observability tapping into Spring Boot Actuator and Prometheus telemetry.

## 💻 Development Setup

To run the frontend independently for development:

1. Ensure the backend is running (`docker compose up db redis redpanda tce-app`).
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite dev server:
   ```bash
   npm run dev
   ```
4. The app will run on `http://localhost:3000` and automatically proxy API requests to `http://localhost:8080`.

## 🎨 Design Philosophy

The UI uses a **Dark Futuristic** theme inspired by premium FinTech infrastructure platforms (Datadog, Stripe, Linear). It utilizes glassmorphism, subtle glow effects, and micro-animations to communicate engineering excellence and operational readiness. This is not a standard CRUD dashboard; it is an engineering platform.
