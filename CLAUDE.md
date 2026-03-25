# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MigraMetric is a full-stack web application for evaluating enterprise heterogeneous system migration workloads. It helps enterprises estimate migration costs and timelines by analyzing system architecture, functional modules, and technology stacks.

## Environment Requirements

- Java 17+ / Maven 3.8+
- Node.js 18+ / pnpm 8+
- MySQL 8.0

## Development Commands

### Backend (migrametric-server)
```bash
cd migrametric-server
mvn spring-boot:run              # Start dev server (port 8080)
mvn test                          # Run all tests
mvn test -Dtest=ClassName        # Run specific test class
mvn test jacoco:report            # Generate coverage report
mvn clean package -DskipTests     # Build JAR
```

### Frontend (migrametric-web)
```bash
cd migrametric-web
pnpm install                      # Install dependencies
pnpm dev                         # Start dev server (port 3000, opens browser)
pnpm build                       # Production build
pnpm lint                        # ESLint check + auto-fix
pnpm type-check                  # TypeScript type checking
pnpm test:unit                    # Run tests (single run)
pnpm test:coverage               # Generate coverage report
```

## Architecture

### Backend (Spring Boot 3.2)
Standard 3-tier architecture: `controller/` -> `service/` -> `mapper/`

- `entity/` — Database entity classes (JPA-style with MyBatis-Plus)
- `dto/` — Request DTOs for API input
- `vo/` — Response View Objects
- `common/` — Result wrapper, ResultCode enum, global exception handler
- `config/` — CORS, Swagger/OpenAPI, JWT properties

Unified response format via `Result<T>` class. Swagger UI at `/swagger-ui.html`.

### Frontend (Vue 3 + TypeScript + Vite)
- `views/` — Route-level page components
- `components/` — Reusable UI components
- `composables/` — Vue Composition API shared logic
- `stores/` — Pinia state management
- `api/` — Axios API modules (one file per backend controller)
- `utils/request.ts` — Axios wrapper with JWT interceptor and error handling
- `router/` — Vue Router with permission guards

Component auto-import is configured via `unplugin-vue-components` and `unplugin-auto-import`.

## Business Logic

### Workload Calculation Model
Core migration workload = Sum of (module base days × weighted coefficient × data volume coefficient × user count coefficient), where coefficients are auto-matched via ladder range matching.

Project lifecycle: Draft → In Progress → Completed → Archived

### 5 Core Modules
1. System Management — system types, module library, ladder configs, report config, user management
2. Project Management — create/list projects, status tracking
3. Workload Evaluation — 4-step evaluation wizard
4. Statistics & Visualization — ECharts dashboards
5. Report Export — Excel/PDF/Word generation

## Key Conventions

- **Soft delete**: All entities use `deleted` field for logical deletion via MyBatis-Plus
- **JWT auth**: Token-based authentication with configurable expiration (default 2 hours)
- **Git workflow**: Feature branches merged into `develop`, then PR to `main`
- **Commit style**: Chinese commit messages describing what was done
- **Current branch**: `develop` (main is the release branch)
- **Development phase**: Currently in Phase 3 (Project Management module)

## Documentation

Comprehensive Chinese documentation in `docs/`:
- `architecture/需求说明文档.md` — Functional requirements
- `architecture/产品设计文档.md` — Product design
- `architecture/技术架构文档.md` — Technical architecture (91KB)
- `architecture/数据库设计文档.md` — Database design (61KB)
- `develop/plan.md` — 7-phase development plan
- `init-db.sql` — Full database schema (44KB)

## API Client (Frontend)

`migrametric-web/src/utils/request.ts` is the Axios instance used by all API modules. It handles:
- JWT token injection from Pinia store
- Global error handling
- Response unwrapping (`Result<T>` wrapper)

API modules in `api/` are named after backend controller packages.
