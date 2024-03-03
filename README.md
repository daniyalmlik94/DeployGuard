# DeployGuard

AI endpoint health monitoring dashboard. Registers LLM provider endpoints (Groq, OpenRouter, mock), probes them on a schedule, records latency / success / token usage, and surfaces the data through a React dashboard.

> **Portfolio project** by [Daniyal Malik](https://github.com/daniyalmlik) — public artifact mirroring DeepAdvisor's AI endpoint validation framework.

---

## What it does

- **Register endpoints** — add Groq, OpenRouter, or mock LLM endpoints with encrypted API keys
- **Scheduled probing** — each endpoint is pinged on a configurable interval; results record latency, tokens, HTTP status
- **Ad-hoc validation run** — trigger an on-demand probe across all endpoints (the "deployment gate" flow)
- **Dashboard** — overview cards, per-endpoint health, latency charts, recent probe timeline

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Maven |
| Database | PostgreSQL 16, Flyway migrations |
| Auth | Spring Security + JWT (HS256) |
| API | REST (writes) + GraphQL (reads) |
| API docs | springdoc-openapi (Swagger UI) |
| Frontend | React 18, Vite, TypeScript, Tailwind CSS |
| GraphQL client | urql |
| Charts | Recharts |
| Tests | JUnit 5, Testcontainers, Vitest |
| CI/CD | GitHub Actions |
| Local orchestration | Docker Compose |

## Architecture

```mermaid
architecture-beta
    group frontend(cloud)[React SPA]
    group backend(server)[Spring Boot Monolith]
    group db(database)[PostgreSQL]

    service ui(internet)[Dashboard] in frontend
    service rest(server)[REST API] in backend
    service gql(server)[GraphQL API] in backend
    service scheduler(server)[Probe Scheduler] in backend
    service providers(internet)[LLM Providers]

    ui:R --> L:rest
    ui:R --> L:gql
    scheduler:B --> T:providers
    rest:B --> T:db
    gql:B --> T:db
    scheduler:B --> T:db
```

## Running locally

```bash
# Requires Docker + Docker Compose
cp .env.example .env
# Fill in GROQ_API_KEY, OPENROUTER_API_KEY, DEPLOYGUARD_ENCRYPTION_KEY

docker compose up --build
# App: http://localhost:3000
# Swagger UI: http://localhost:8080/swagger-ui.html
# GraphiQL: http://localhost:8080/graphiql
```

## Key design decisions

- **Monolith** — single domain, single admin, fast iteration. Transactions stay simple inside one Postgres instance.
- **REST + GraphQL split** — REST for CRUD writes (Swagger-friendly, standard mutation semantics); GraphQL for dashboard reads (client-shaped queries, flexible time windows).
- **`SELECT ... FOR UPDATE SKIP LOCKED`** — scheduled probe picker is safe to run across multiple app instances without a distributed lock service.
- **AES-GCM API key encryption** — keys encrypted at rest, encryption key from env var (in prod: Azure Key Vault / AWS Secrets Manager). Never logged, never returned in payloads.
- **In-process mock provider** — `/mock-llm/v1/chat/completions` with configurable latency/failure rate. Makes the demo work without depending on a real broken service.

See `docs/decisions/` for full ADRs.

## What's intentionally out of scope

Teams/multi-tenancy, OAuth/SSO, alerting, Kafka, Kubernetes, prompt execution features, vector embeddings.

---

*CI badge will appear here after first push.*
