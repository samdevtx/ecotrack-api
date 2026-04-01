# Projeto - EcoTrack Mobilidade Sustentável

## Descrição

O EcoTrack é uma aplicação desenvolvida em Spring Boot que permite aos usuários rastrear suas viagens e calcular a pegada de carbono associada a diferentes meios de transporte. A aplicação oferece insights personalizados sobre sustentabilidade e permite a compensação de carbono através de projetos ambientais.

## Funcionalidades

- **Gestão de Usuários**: Cadastro, autenticação e autorização com JWT
- **Rastreamento de Viagens**: Registro de viagens com diferentes meios de transporte
- **Cálculo de Pegada de Carbono**: Cálculo automático baseado no tipo de transporte e distância
- **Compensação de Carbono**: Sistema de compensação através de projetos ambientais
- **Insights com IA**: Análise de dados e sugestões personalizadas usando Hugging Face
- **API RESTful**: Endpoints documentados com Swagger/OpenAPI

## Como executar localmente com Docker

### Pré-requisitos

- Docker Desktop instalado
- Docker Compose
- Git
- 8GB de RAM disponível
- Portas 8080, 1521, 3000, 9090 livres

### Passos para execução

1. **Clone o repositório**
```bash
git clone <repository-url>
cd mobilidade-sustentavel
```

2. **Configure as variáveis de ambiente**
```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite o arquivo .env com suas configurações
# JWT_SECRET=sua_chave_secreta_jwt
# SPRING_SECRET_AI_KEY=sua_chave_hugging_face
```

3. **Execute com Docker Compose (Desenvolvimento)**
```bash
# Para ambiente de desenvolvimento
docker-compose -f docker-compose.dev.yml up -d

# Para ambiente completo com monitoramento
docker-compose up -d
```

4. **Verifique se os serviços estão rodando**
```bash
docker-compose ps
```

5. **Acesse a aplicação**
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health
- **Grafana**: http://localhost:3000 (credentials set via `GRAFANA_PASSWORD` in `.env`)
- **Prometheus**: http://localhost:9090

6. **Para parar os serviços**
```bash
docker-compose down
```

### Comandos úteis

```bash
# Ver logs da aplicação
docker-compose logs -f app

# Rebuild da aplicação
docker-compose build app

# Executar apenas banco de dados
docker-compose up oracle-db

# Limpar volumes (CUIDADO: apaga dados)
docker-compose down -v
```

## Pipeline CI/CD

### Ferramentas Utilizadas

- **GitHub Actions**: Plataforma de CI/CD integrada ao GitHub
- **Docker**: Containerização da aplicação
- **Maven**: Build e gerenciamento de dependências
- **Oracle Database**: Banco de dados para testes de integração

### Etapas do Pipeline

O pipeline está configurado no arquivo `.github/workflows/ci-cd.yml` e inclui as seguintes etapas:

#### 1. **Build & Test** 🧪
- Checkout do código
- Setup do JDK 17
- Cache das dependências Maven
- Compilação da aplicação
- Execução dos testes unitários e de integração
- Geração de relatórios de teste
- Upload dos artefatos de build

#### 2. **Code Analysis** 🔍
- Análise estática de código com SpotBugs
- Verificação de vulnerabilidades com OWASP Dependency Check
- Análise de qualidade de código

#### 3. **Build & Push Docker Image** 🐳
- Build da imagem Docker multi-arquitetura (AMD64/ARM64)
- Push para GitHub Container Registry
- Versionamento automático das imagens
- Cache otimizado para builds rápidos

#### 4. **Deploy to Staging** 🚀
- Deploy automático para ambiente de staging
- Testes de smoke no ambiente
- Verificação de saúde da aplicação

#### 5. **Deploy to Production** 🌟
- Deploy para produção (apenas branch main)
- Verificações pós-deploy
- Health checks automatizados

#### 6. **Notifications** 📢
- Notificações sobre status do pipeline
- Relatórios de sucesso/falha

### Funcionamento

1. **Trigger**: O pipeline é executado automaticamente em:
   - Push para branches `main` ou `develop`
   - Pull requests para `main`

2. **Ambientes**:
   - **Staging**: Deploy automático para testes
   - **Production**: Deploy manual com aprovação

3. **Segurança**:
   - Secrets gerenciados pelo GitHub
   - Análise de vulnerabilidades
   - Imagens assinadas e verificadas

## Containerização

### Dockerfile

O Dockerfile utiliza uma abordagem multi-stage para otimizar o tamanho e segurança da imagem:

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
# ... build da aplicação

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
# ... configuração do runtime
```

### Estratégias Adotadas

#### **Multi-stage Build**
- **Stage 1**: Build da aplicação com JDK completo
- **Stage 2**: Runtime com JRE mínimo
- Redução de ~60% no tamanho da imagem final

#### **Otimizações de Performance**
- Cache de dependências Maven
- Layers otimizados para Docker cache
- JVM tuning para containers
- Health checks integrados

#### **Segurança**
- Usuário não-root (appuser:1001)
- Imagem base Alpine (menor superfície de ataque)
- Atualizações de segurança automáticas
- Secrets não expostos na imagem

#### **Monitoramento**
- Health checks configurados
- Logs estruturados
- Métricas expostas via Actuator
- Integração com Prometheus

### Docker Compose

#### **Arquitetura dos Serviços**

1. **oracle-db**: Banco de dados Oracle XE
2. **app**: Aplicação Spring Boot
3. **redis**: Cache distribuído
4. **nginx**: Reverse proxy e load balancer
5. **prometheus**: Coleta de métricas
6. **grafana**: Visualização de métricas

#### **Comandos Utilizados**

```bash
# Build e start completo
docker-compose up --build

# Apenas serviços essenciais
docker-compose up oracle-db app

# Desenvolvimento com hot reload
docker-compose -f docker-compose.dev.yml up

# Produção com monitoramento
docker-compose -f docker-compose.yml up
```

#### **Imagem Criada**

- **Nome**: `ghcr.io/[username]/mobilidade-sustentavel`
- **Tags**: `latest`, `main-[sha]`, `develop-[sha]`
- **Tamanho**: ~200MB (otimizada)
- **Arquiteturas**: AMD64, ARM64

## Tecnologias utilizadas

### **Backend Framework**
- **Spring Boot 3.3.1**: Framework principal
- **Spring Security**: Autenticação e autorização
- **Spring Data JPA**: Persistência de dados
- **Spring WebFlux**: Cliente HTTP reativo
- **Spring Boot Actuator**: Monitoramento e métricas

### **Linguagem e Runtime**
- **Java 17**: Linguagem de programação
- **Maven**: Gerenciamento de dependências
- **JUnit 5**: Testes unitários

### **Banco de Dados**
- **Oracle Database XE 21c**: Banco principal
- **Flyway**: Migração de schema
- **HikariCP**: Pool de conexões

### **Segurança**
- **JWT (JJWT)**: Tokens de autenticação
- **BCrypt**: Hash de senhas
- **HTTPS/TLS**: Comunicação segura

### **Documentação**
- **SpringDoc OpenAPI**: Documentação da API
- **Swagger UI**: Interface interativa

### **DevOps e Infraestrutura**
- **Docker**: Containerização
- **Docker Compose**: Orquestração local
- **GitHub Actions**: CI/CD
- **Nginx**: Reverse proxy
- **Redis**: Cache distribuído

### **Monitoramento**
- **Prometheus**: Coleta de métricas
- **Grafana**: Visualização de dados
- **Micrometer**: Métricas da aplicação

### **Integração Externa**
- **Hugging Face API**: Inteligência artificial
- **WebClient**: Cliente HTTP

### **Qualidade de Código**
- **SpotBugs**: Análise estática
- **OWASP Dependency Check**: Verificação de vulnerabilidades
- **SonarQube**: Análise de qualidade (configurável)

## Pré-requisitos

*   Docker Engine e Docker Compose
*   Arquivo `.env` na raiz do projeto (veja abaixo)

## Arquivo `.env`

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
JWT_SECRET=seu_segredo_jwt_super_forte_e_seguro_com_pelo_menos_256_bits
JWT_EXPIRATION_MS=3600000 # Ex: 1 hora
SPRING_SECRET_AI_KEY=sua_chave_api_hugging_face_aqui
```
*   `JWT_SECRET`: Chave para assinar os tokens JWT. Use um valor longo e aleatório.
*   `JWT_EXPIRATION_MS`: Tempo de expiração do token em milissegundos.
*   `SPRING_SECRET_AI_KEY`: Sua chave de API da Hugging Face para o serviço de insights.

## Executando com Docker Compose

1.  **Construir e Iniciar os Serviços:**
    Na raiz do projeto, execute:
    ```bash
    docker-compose up --build
    ```
    Isso irá construir a imagem da aplicação e iniciar os containers da aplicação e do banco de dados Oracle.

2.  **Acessando a Aplicação:**
    *   API: `http://localhost:8080`
    *   Swagger UI (Documentação da API): `http://localhost:8080/swagger-ui.html`

3.  **Parar os Serviços:**
    ```bash
    docker-compose down
    ```

## Usuários Padrão (Criados na Primeira Execução)

*   **Admin:** `admin@ecotrack.com`
*   **Usuário Comum:** `user@ecotrack.com`

Passwords are set via environment variables. See `.env.example`.

## Observações

*   O serviço `oracle-db` cria automaticamente o usuário `ecotrack` a partir da variável `ORACLE_PASSWORD` definida no `.env`.
*   As migrações do banco de dados (Flyway) são aplicadas automaticamente na inicialização.