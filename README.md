# Artistas API

API REST para gestão de artistas e álbuns (relacionamento N:N), com autenticação JWT/Basic Auth, upload de imagens via MinIO (Presigned URLs), WebSocket para notificações em tempo real e sincronização de regionais com API externa. Stack: **Spring Boot 3.3**, **Java 17**, **PostgreSQL**, **Flyway**, **MinIO**, **Docker**.

---

## Índice

- [Dados do processo seletivo](#-dados-do-processo-seletivo)
- [Descrição](#-descrição)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Início rápido](#-início-rápido)
- [Autenticação](#-autenticação)
- [Endpoints](#-endpoints)
- [Upload e armazenamento](#-upload-e-armazenamento)
- [Banco de dados](#-banco-de-dados)
- [Observabilidade](#-observabilidade)
- [Testes](#-testes)
- [Documentação da API](#-documentação-da-api)
- [Variáveis de ambiente](#-variáveis-de-ambiente)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Próximos passos técnicos](#-próximos-passos-técnicos)

---

## Dados do processo seletivo

| Campo | Valor |
|-------|--------|
| **Processo** | Processo Seletivo SEPLAG |
| **Vaga** | (conforme edital) |
| **Candidato** | Adriano Alves Ribeiro |
| **Identificador** | adrianoalvesribeiro700010 |

---

## Descrição

Sistema de **artistas** e **álbuns** com vínculo **N:N** (tabela `artista_album`). Inclui:

- **CRUD** de artistas e álbuns e associação entre eles
- **Upload de imagens** de capa em MinIO com **Presigned URLs** (acesso temporário, sem expor credenciais)
- **Autenticação** em duas estratégias: **v1** (login JSON + JWT) e **v2** (Basic Auth + JWT), com refresh token
- **Rate limiting** (Bucket4j): 10 requisições/minuto por usuário autenticado ou por IP
- **WebSocket** (STOMP): notificação em tempo real no tópico `/topic/novo-album` ao cadastrar álbum
- **Regionais**: sincronização com API externa via **OpenFeign** (na subida da aplicação e via endpoint manual)
- **Health checks** (Actuator) com indicador customizado para MinIO
- **OpenAPI/Swagger** para documentação e testes da API

O banco é **pré-populado** pela migration V2 (artistas e álbuns de exemplo), permitindo testar listagens, filtros e vínculos sem cadastro manual.

---

## Arquitetura

Organização em **camadas** alinhada ao ecossistema Spring:

| Camada | Responsabilidade |
|--------|-------------------|
| **Controller** | REST e WebSocket; versionamento em `/api/v1` (auth em `/api/v2` onde aplicável) |
| **Service** | Regras de negócio, orquestração, MinIO, JWT, notificações STOMP |
| **Repository** | Acesso a dados (Spring Data JPA) |
| **Model / DTO** | Entidades JPA e contratos de entrada/saída |
| **Config** | Segurança, CORS, OpenAPI, MinIO, WebSocket, agendamento |
| **Client** | Integração externa (OpenFeign) |
| **Security** | Filtros (JWT, Basic Auth, Rate Limit), sessão **stateless** |
| **Exception** | Tratamento global (`@RestControllerAdvice`): validação (400), não encontrado (404), conflito (409) |

**Decisões técnicas:**

- **Flyway** para evolução do schema; JPA com `ddl-auto=validate` — sem geração automática de DDL, garantindo histórico e reprodutibilidade.
- **MinIO** como objeto storage; acesso somente por **Presigned URLs** com TTL definido (visualização e download), sem expor endpoint interno.
- **Nginx** na frente do MinIO em Docker: roteamento em `/minio` e reescrita das Presigned URLs para o host público, evitando expor a porta do MinIO.
- **Refresh token** em memória (`RefreshTokenStore`): adequado a single-instance; em ambiente escalado, migrar para store distribuído (ex.: Redis).
- **Regionais**: modelo ativo/inativo e sincronização com API externa; alterações de nome geram novo registro ativo e inativação do anterior, preservando histórico.

---

## Tecnologias

| Categoria | Tecnologia |
|-----------|------------|
| Linguagem / Runtime | Java 17 |
| Framework | Spring Boot 3.3.6 (Web, Data JPA, Security, Actuator, WebSocket, Validation) |
| Integração HTTP | Spring Cloud OpenFeign |
| Banco de dados | PostgreSQL 15 |
| Migrations | Flyway |
| Armazenamento de objetos | MinIO (S3-compatível) |
| Segurança | Spring Security, JWT (jjwt 0.11.5), Basic Auth |
| Rate limiting | Bucket4j 8.0.1 |
| Documentação | SpringDoc OpenAPI 2.6 (Swagger UI) |
| Build | Maven 3.9+ |
| Infraestrutura | Docker, Docker Compose, Nginx |

---

## Pré-requisitos

- **Java 17** (JDK)
- **Maven 3.9+** ou wrapper do projeto (`./mvnw` / `mvnw.cmd`)
- **Docker** e **Docker Compose** (para execução em containers)
- Portas disponíveis: **5432** (PostgreSQL), **8080** (API), **80** (Nginx), **9000** e **9001** (MinIO)

---

## Início rápido

### Opção 1: Tudo em Docker (recomendado)

```bash
docker compose up -d --build
```

- API: **http://localhost:8080**
- Swagger: **http://localhost:8080/swagger-ui.html**
- Health: **http://localhost:8080/actuator/health**
- MinIO Console: **http://localhost:9001** (minioadmin / minioadmin)

Parar e remover volumes:

```bash
docker compose down -v --remove-orphans
```

### Opção 2: Apenas dependências em Docker, API local

1. Subir Postgres e MinIO:

   ```bash
   docker compose up -d postgres minio
   ```

2. Em `application.properties`, usar conexão local, por exemplo:
   - `spring.datasource.url=jdbc:postgresql://localhost:5432/artistasdb`
   - `minio.url=http://localhost:9000`
   - Ajustar `minio.public-url` se for usar Nginx (ex.: `http://localhost/minio`).

3. Executar a aplicação:

   ```bash
   ./mvnw clean spring-boot:run
   ```

### Serviços (Docker Compose)

| Serviço | Container | Porta(s) | Função |
|---------|-----------|----------|--------|
| artistas-api | artistas-api | 8080 | API Spring Boot |
| postgres | postgres_db | 5432 | PostgreSQL |
| minio | minio | 9000, 9001 | Objeto storage + console |
| nginx | nginx_proxy | 80 | Proxy para MinIO em `/minio` |

Credenciais padrão do banco: usuário e senha `postgres`, banco `artistasdb`.

---

## Autenticação

Endpoints de negócio exigem autenticação (Bearer JWT ou Basic Auth). Públicos: login, refresh, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`, WebSocket (`/ws`, `/topic/**`, `/app/**`).

### v1 — Login JSON + JWT

| Ação | Método e endpoint | Corpo / Cabeçalho |
|------|-------------------|--------------------|
| Login | `POST /api/v1/auth/login` | Body: `{ "username": "seletivo", "password": "admin" }` |
| Refresh | `POST /api/v1/auth/refresh` | Body: `{ "refreshToken": "<token>" }` |
| Chamadas protegidas | Qualquer | `Authorization: Bearer <accessToken>` |

### v2 — Basic Auth + JWT

| Ação | Método e endpoint | Corpo / Cabeçalho |
|------|-------------------|--------------------|
| Login | `POST /api/v2/auth/login` | `Authorization: Basic <base64(username:password)>` |
| Refresh | `POST /api/v2/auth/refresh` | Body: `{ "refreshToken": "<token>" }` |
| Chamadas protegidas | Qualquer | `Authorization: Bearer <accessToken>` ou `Authorization: Basic ...` |

**Credenciais padrão:** `seletivo` / `admin` (configuráveis em `app.security.username` e `app.security.password`).

**JWT:** Access token com vida curta (ex.: 5 min); refresh token com vida maior (ex.: 30 min). Refresh tokens são armazenados em memória; o endpoint de refresh exige token válido e presença no store.

---

## Endpoints

Base: `http://localhost:8080`. Todos os recursos abaixo exigem autenticação, exceto os de auth e os listados como públicos.

| Recurso | Método | Endpoint | Descrição |
|---------|--------|----------|-----------|
| **Auth v1** | POST | `/api/v1/auth/login` | Login JSON → accessToken, refreshToken |
| | POST | `/api/v1/auth/refresh` | Novo accessToken |
| **Auth v2** | POST | `/api/v2/auth/login` | Login Basic Auth → tokens |
| | POST | `/api/v2/auth/refresh` | Novo accessToken |
| **Artistas** | POST | `/api/v1/artistas` | Criar (nome, tipo: CANTOR \| BANDA) |
| | GET | `/api/v1/artistas` | Listar paginado; query `nome` opcional |
| | PUT | `/api/v1/artistas/{id}` | Atualizar |
| | DELETE | `/api/v1/artistas/{id}` | Excluir |
| **Álbuns** | POST | `/api/v1/album` | Criar |
| | GET | `/api/v1/album` | Listar paginado; query `nome` opcional |
| | GET | `/api/v1/album/{id}` | Buscar por ID |
| | GET | `/api/v1/album/artista/{id}` | Álbuns do artista |
| | GET | `/api/v1/album/tipo-artista` | Query `tipo`: CANTOR \| BANDA |
| | GET | `/api/v1/album/album-por-artista` | Query `artista`: nome (parcial) |
| | PUT | `/api/v1/album/{id}` | Atualizar |
| | DELETE | `/api/v1/album/{id}` | Excluir |
| **Vínculo** | POST | `/api/v1/artistas-albuns` | Vincular artista e álbum (body: `artistaId`, `albumId`) |
| **Imagens** | POST | `/api/v1/albuns/{albumId}/imagens` | Upload multipart (parte `files`) |
| | GET | `/api/v1/albuns/{albumId}` | Listar imagens do álbum (URLs Presigned) |
| | GET | `/api/v1/albuns/{imagemId}/download` | URL Presigned para download |
| | PUT | `/api/v1/albuns/{imagemId}` | Substituir imagem (multipart `file`) |
| | DELETE | `/api/v1/albuns/{imagemId}` | Remover imagem |
| **Regionais** | GET | `/api/v1/regionais` | Listar ativas |
| | GET | `/api/v1/regionais/{regionalId}` | Buscar por ID de negócio |
| | POST | `/api/v1/regionais` | Criar |
| | PUT | `/api/v1/regionais/{regionalId}` | Atualizar |
| | DELETE | `/api/v1/regionais/{regionalId}` | Desativar |
| | POST | `/api/v1/regionais/sincronizar` | Disparar sincronização com API externa |

---

## Upload e armazenamento

- **MinIO** (S3-compatível): bucket definido em `minio.bucket` (ex.: `albuns`), criado na subida da aplicação (`MinioBucketInitializer`).
- **Upload:** multipart no endpoint de imagens; arquivo salvo no MinIO com nome único (UUID + nome original); na tabela `album_imagens` persiste-se o **nome do objeto** (campo `url`), não a URL pública.
- **Acesso:** apenas **Presigned URLs** (GET), com TTL limitado (ex.: 30 min para visualização, 10 min para download com `response-content-disposition: attachment`). Nenhuma URL direta ao MinIO é exposta.
- **Nginx:** em Docker, o cliente acessa o MinIO via `http://localhost/minio` (ou host do Nginx); as Presigned URLs são reescritas para esse host para não expor a porta 9000.
- **Respostas:** listagem de imagens retorna `id` e `url` (Presigned); o endpoint de download retorna JSON `{ "downloadUrl": "..." }`.

---

## Banco de dados

- **PostgreSQL 15**; JPA com `ddl-auto=validate`; schema 100% controlado por **Flyway** em `src/main/resources/db/migration/` (convenção `V{n}__nome.sql`).

### Migrations

| Versão | Arquivo | Conteúdo |
|--------|---------|----------|
| V1 | `V1__criar_tabelas.sql` | `artista`, `album`, `artista_album` |
| V2 | `V2__insert_artistas_albuns.sql` | Carga inicial: artistas e álbuns para testes |
| V3 | `V3__create_table_album_imagens.sql` | `album_imagens` (FK `album_id`, ON DELETE CASCADE) |
| V4 | `V4__create_regionais_table.sql` | `regionais` |

### Modelo de dados

| Tabela | Colunas principais | Observação |
|--------|--------------------|------------|
| **artista** | `id` (PK), `nome`, `tipo` (VARCHAR: CANTOR, BANDA) | |
| **album** | `id` (PK), `nome` | |
| **artista_album** | `artista_id` (PK, FK), `album_id` (PK, FK) | N:N |
| **album_imagens** | `id` (PK), `url` (nome do objeto no MinIO), `album_id` (FK, CASCADE) | |
| **regionais** | `id` (PK), `regional_id` (UNIQUE, ID de negócio), `nome`, `ativo`, `data_criacao` | Modelo ativo/inativo |

A **carga inicial (V2)** insere artistas (ex.: Serj Tankian, Mike Shinoda, Michel Teló, Guns N' Roses) e álbuns, permitindo testar listagens, filtros e vínculos sem cadastro manual.

---

## Observabilidade

- **Actuator:** endpoints `health` e `info` expostos; health com detalhes (`show-details=always`).
- **Health customizado:** `MinioHealthIndicator` executa `listBuckets` no MinIO e inclui o status (UP/DOWN) no health agregado.
- **Monitor WebSocket:** página estática `/monitor.html` subscreve o tópico `/topic/novo-album` e exibe notificações de novos álbuns; o acesso à página exige **Basic Auth** (tratado por `JwtAuthenticationEntryPoint`).

---

## Testes

Testes em `src/test/java` (JUnit, Spring Boot Test).

```bash
./mvnw test
```

Principais conjuntos: **controller** (Artista, Album, AlbumImagem, Auth v2), **service** (Artista, Album, AlbumImagem, JWT). O build do Docker usa `-DskipTests`; para rodar testes no build, use `./mvnw clean package` sem `-DskipTests`.

---

## Documentação da API

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **OpenAPI (JSON):** http://localhost:8080/v3/api-docs  

Schemas de segurança **Bearer JWT** e **Basic Auth** configurados em `OpenApiConfig`; endpoints protegidos anotados com o requisito correspondente.

---

## Variáveis de ambiente

Principais variáveis (Docker Compose e/ou `application.properties`).

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| **Banco** | | |
| `SPRING_DATASOURCE_URL` | URL JDBC | `jdbc:postgresql://postgres:5432/artistasdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuário | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha | `postgres` |
| **MinIO** | | |
| `minio.url` / `MINIO_ENDPOINT` | Endpoint interno | `http://minio:9000` |
| `minio.access-key` / `MINIO_ACCESS_KEY` | Access key | `minioadmin` |
| `minio.secret-key` / `MINIO_SECRET_KEY` | Secret key | `minioadmin` |
| `minio.bucket` | Nome do bucket | `albuns` |
| `minio.public-url` / `MINIO_PUBLIC_URL` | Base URL pública (proxy) | `http://localhost/minio` |
| **JWT** | | |
| `app.jwt.secret` | Chave HMAC (mín. 32 caracteres) | (string segura) |
| `app.jwt.access-expiration` | Expiração access token (ms) | `300000` |
| `app.jwt.refresh-expiration` | Expiração refresh token (ms) | `1800000` |
| **Aplicação** | | |
| `app.security.username` | Usuário de login | `seletivo` |
| `app.security.password` | Senha de login | `admin` |
| `COMPOSE_PROJECT_NAME` | Nome do projeto Compose | `adrianoalvesribeiro700010` |

---

## Estrutura do projeto

```
src/main/java/com/adrianoribeiro/artistas_api/
├── ArtistasApiApplication.java       # @SpringBootApplication, @EnableFeignClients
├── client/
│   └── RegionalClient.java           # Feign: GET /v1/regionais (API externa)
├── config/
│   ├── JwtAuthenticationEntryPoint.java  # 401 API (JSON) e /monitor.html (Basic)
│   ├── MinioBucketInitializer.java   # Cria bucket na subida
│   ├── MinioConfig.java              # Bean MinioClient
│   ├── OpenApiConfig.java            # OpenAPI: bearerAuth, basicAuth
│   ├── SchedulerConfig.java          # @Scheduled: sync regionais (delay 3s)
│   └── WebSocketConfig.java          # STOMP /ws, broker /topic, prefix /app
├── controller/
│   ├── AlbumController.java          # CRUD e listagens de álbum
│   ├── AlbumImagemController.java    # Imagens: upload, listagem, download
│   ├── ArtistaAlbumController.java   # Vínculo artista-álbum
│   ├── ArtistaController.java       # CRUD artistas
│   ├── RegionalController.java      # CRUD regionais
│   ├── RegionalSyncController.java  # POST /sincronizar
│   ├── v1/AuthControllerV1.java      # Auth v1 (login JSON)
│   └── v2/AuthControllerV2.java      # Auth v2 (Basic Auth)
├── dto/                              # Request/Response DTOs
├── exception/
│   └── GlobalExceptionHandler.java   # 400, 404, 409
├── health/
│   └── MinioHealthIndicator.java     # Health do MinIO
├── model/                            # Entidades JPA + TipoArtista (enum)
├── repository/                       # JpaRepository
├── security/
│   ├── JwtAuthenticationFilter.java  # Bearer JWT + Basic Auth
│   ├── RefreshTokenStore.java       # Store em memória (refresh tokens)
│   ├── SecurityConfig.java          # CORS, stateless, filtros, regras
│   └── ratelimit/RateLimitFilter.java # Bucket4j, 10 req/min
├── service/                          # Regras de negócio
└── websocket/
    └── AlbumNotificationService.java # Publica em /topic/novo-album
```

```
src/main/resources/
├── application.properties
├── db/migration/                     # Flyway V1–V4
└── static/monitor.html              # Página monitor WebSocket
```

---

## Próximos passos técnicos

Sugestões de evolução com base no código atual:

| Área | Proposta | Motivo |
|------|----------|--------|
| Refresh token | Store distribuído (ex.: Redis) ou persistência | Suportar múltiplas instâncias e revogação explícita |
| Rate limit | Parametrizar limite/janela (config) e políticas por endpoint | Flexibilidade e proteção granular |
| Regionais | URL da API externa via env; retry + circuit breaker (Resilience4j) | Resiliência e configuração por ambiente |
| MinIO | Unificar `minio.public-url` por perfil (local/docker) | Evitar divergência entre ambientes |
| Testes | Integração com Testcontainers (PostgreSQL, MinIO); testes de segurança e fluxo Presigned URL | Maior confiança em deploy |
| API | Versionar recursos (ex.: `/api/v2/artistas`) e deprecar v1 de forma controlada | Evolução sem quebrar clientes |
| Logs | Substituir `System.out` por logger (ex.: SLF4J) | Rastreabilidade e níveis de log |
| OpenAPI | Exemplos de request/response e documentação dos códigos de erro no `GlobalExceptionHandler` | Melhor experiência para integradores |
