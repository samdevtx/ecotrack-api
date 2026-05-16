# EcoTrack — Mobilidade Sustentável

[![CI/CD](https://github.com/samdevtx/ecotrack-api/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/samdevtx/ecotrack-api/actions/workflows/ci-cd.yml)

API REST em Spring Boot para rastreamento de viagens e cálculo de pegada de carbono. Usuários registram deslocamentos por modal de transporte, recebem cálculos automáticos de CO₂ e insights personalizados de sustentabilidade via Hugging Face.

**Live:** [https://ecotrack-api.samdevtx.me/swagger-ui.html](https://ecotrack-api.samdevtx.me/swagger-ui.html)

---

## Funcionalidades

- **Gestão de Usuários** — cadastro, autenticação e autorização com JWT
- **Rastreamento de Viagens** — registro por modal (carro, moto, ônibus, metrô, bicicleta, etc.)
- **Cálculo de Pegada de Carbono** — fatores de emissão por km configuráveis por tipo de transporte
- **Compensação de Carbono** — associação de viagens a projetos ambientais
- **Insights com IA** — análise e sugestões personalizadas via Hugging Face API
- **API RESTful** — endpoints documentados com Swagger/OpenAPI

---

## Stack

| Camada | Tecnologia |
|---|---|
| Framework | Spring Boot 3.5.13 + Java 17 |
| Segurança | Spring Security + JWT (JJWT) + BCrypt |
| Persistência | PostgreSQL 17 + Spring Data JPA + Flyway + HikariCP |
| HTTP reativo | Spring WebFlux (WebClient) |
| IA | Hugging Face API |
| Monitoramento | Spring Actuator + Prometheus + Grafana + Micrometer |
| Resiliência | Resilience4j (circuit breaker) |
| Erros | Sentry |
| Docs | SpringDoc OpenAPI + Swagger UI |
| Qualidade | SpotBugs + OWASP Dependency Check |
| Infra | Docker (multi-stage) + Docker Compose + Nginx |

---

## Executar localmente

### Pré-requisitos

- Docker Desktop
- Git
- Portas `8080`, `8081`, `3000`, `9090` livres

### Setup

```bash
git clone https://github.com/samdevtx/ecotrack-api.git
cd ecotrack-api

cp .env.example .env
# Preencha JWT_SECRET, SPRING_SECRET_AI_KEY e DB_PASSWORD no .env

# Desenvolvimento (com hot reload)
docker compose -f docker-compose.yml -f docker-compose.dev.yml up

# Stack completa com monitoramento
docker compose up -d
```

### Endpoints após subir

| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8081/actuator/health |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

### Usuários padrão

| Tipo | Email |
|---|---|
| Admin | admin@ecotrack.com |
| Usuário | user@ecotrack.com |

Senhas definidas via variável de ambiente — veja `.env.example`.

---

## Makefile

```bash
make dev    # docker compose com hot reload
make stack  # docker compose completo
make test   # ./mvnw verify
make build  # ./mvnw package -DskipTests
make lint   # SpotBugs + Checkstyle
make clean  # mvn clean + docker compose down -v
```

---

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `JWT_SECRET` | Chave de assinatura JWT (mínimo 256 bits) |
| `JWT_EXPIRATION_MS` | Expiração do token em ms (ex: `3600000`) |
| `SPRING_SECRET_AI_KEY` | Chave da API Hugging Face |
| `DB_PASSWORD` | Senha do PostgreSQL |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas no CORS |
| `GRAFANA_PASSWORD` | Senha do admin do Grafana |
| `SENTRY_DSN` | DSN do Sentry para rastreamento de erros |

---

## Pipeline CI/CD

Configurado em `.github/workflows/ci-cd.yml`:

1. **Build & Test** — compilação + testes unitários com PostgreSQL via service container
2. **Code Analysis** — SpotBugs (análise estática) + OWASP Dependency Check (CVEs)
3. **Build & Push** — imagem Docker para GitHub Container Registry (`ghcr.io`) com SHA tag
4. **Trivy Scan** — varredura de vulnerabilidades HIGH/CRITICAL na imagem publicada
5. **Deploy Staging** — deploy automático ao push em `develop`
6. **Deploy Production** — deploy automático ao push em `main` (com aprovação via GitHub environment)

Trigger: push em `main`/`develop` e pull requests para `main`.

---

## Arquitetura dos containers

```
docker-compose.yml
├── postgres       — PostgreSQL 16 (banco principal)
├── app            — Spring Boot (porta 8080 / management 8081)
├── redis          — cache distribuído
├── nginx          — reverse proxy
├── prometheus     — coleta de métricas
└── grafana        — dashboards (config em config/grafana/)
```

---

## Estrutura do projeto

```
src/
  main/
    java/.../
      controller/   — HTTP: validação, delegação ao service, resposta
      service/      — lógica de negócio
      repository/   — acesso a dados (JPA + Specifications)
      dto/          — request/response + mappers
      model/        — entidades JPA
      security/     — JWT filter, UserDetailsService, SecurityConfig
      config/       — CO2 factors, WebClient, DataInitializer
    resources/
      db/migration/ — scripts Flyway
      application.yml
  test/
    java/.../
      service/      — testes unitários dos services (Mockito)
      security/     — testes do TokenService e UserDetailsServiceImpl
```

---

## Arquivo `.http`

`ecotrack-api.http` na raiz contém requests prontos para VS Code REST Client e IntelliJ HTTP Client — autenticação, CRUD de usuários, viagens, compensações e insights.
