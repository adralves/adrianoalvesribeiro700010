## Dados do processo seletivo

| Campo            | Valor                                                                    |
|------------------|--------------------------------------------------------------------------|
| **Edital**       | Processo Seletivo CONJUNTO Nº 001/2026/ SEPLAG                           |
| **cargo**        | Analista de Tecnologia da Informação - Engenheiro da Computação - Sênior |
| **Candidato**    | Adriano Alves Ribeiro                                                    |
| **N° Inscrição** | 16383                                                                    |
 | **Projeto**      | ANEXO II- A - Projeto Desenvolvedor Back End                             |

---

# Artistas API

API REST para gestão de artistas e álbuns (relacionamento N:N), com autenticação JWT/Basic Auth, upload de imagens via MinIO (Presigned URLs), WebSocket para notificações em tempo real e sincronização de regionais com API externa. Stack: **Spring Boot 3.3**, **Java 17**, **PostgreSQL**, **Flyway**, **MinIO**, **Docker**.

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

## 🏛️ Arquitetura do Projeto

O projeto segue o padrão de **Arquitetura em Camadas (Layered Architecture)**, garantindo a separação de responsabilidades e facilitando a manutenção. A comunicação é baseada no modelo **Stateless** (sem estado no servidor), o que permite alta escalabilidade em ambientes conteinerizados.

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
| **WebSocket** | Infraestrutura de mensageria para broadcast de métricas e eventos. |
| **Infraestrutura (Docker)** | Orquestração do ambiente e gerenciamento de variáveis de configuração. |

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

## Como testar
### Opção 1: Tudo em Docker (recomendado)  

Clone o repositorio  
```
git clone https://github.com/adralves/adrianoalvesribeiro700010.git
cd adrianoalvesribeiro700010/
```
Navegue ate a pasta do projeto "adrianoalvesribeiro700010"


(Recomendado) Limpe o cache/volumes do Docker antes de subir, para evitar conflitos de banco/porta:

Execute o seguinte comando no terminal dentro dessa pasta:
```bash
docker compose up -d --build
```

- API: **http://localhost:8080**
- Swagger: **http://localhost:8080/swagger-ui.html**
- Health: **http://localhost:8080/actuator/health**
- Liveness **http://localhost:8080/actuator/health/liveness)**
- Readiness: **http://localhost:8080/actuator/health/readiness**
- MinIO Console: **http://localhost:9001** (minioadmin / minioadmin)


### Serviços (Docker Compose)

| Serviço | Container | Porta(s) | Função |
|---------|-----------|----------|--------|
| artistas-api | artistas-api | 8080 | API Spring Boot |
| postgres | postgres_db | 5432 | PostgreSQL |
| minio | minio | 9000, 9001 | Objeto storage + console |
| nginx | nginx_proxy | 80 | Proxy para MinIO em `/minio` |

Credenciais padrão do banco: usuário e senha `postgres`, banco `artistasdb`.

---

## 🔐 Guia de Autenticação (Híbrida)

Endpoints de negócio exigem autenticação (Bearer JWT ou Basic Auth). Públicos: login, refresh, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`, WebSocket (`/ws`, `/topic/**`, `/app/**`).  

O projeto implementa dois fluxos de autenticação para demonstrar versionamento de API e flexibilidade de segurança.  

### Fluxo Recomendado: Autenticação V2 (Híbrida)

Este fluxo utiliza **Basic Auth** para a troca inicial e **JWT** para as chamadas subsequentes.

1.  **Autorização Inicial `basicAuth` (Basic):** * No topo do Swagger, clique no botão **Authorize**.

    -   Em `basicAuth (http, Basic)`, informe:

        -   **Username:** `seletivo`

        -   **Password:** `admin`

    -   Clique em **Authorize** e **Close**.

2.  **Obtenção do Token JWT:**

    -   Vá ao endpoint `POST /api/v2/auth/login`.

    -   Execute a requisição (não precisa de body, pois ele usará suas credenciais Basic).

    -   Copie o `accessToken` retornado.

3.  **Autorização Final (Bearer):**

    -   Clique novamente em **Authorize** no topo da página.

    -   No campo `bearerAuth (http, Bearer)`, cole o seu `accessToken`.

    -   Clique em **Authorize** e **Close**.
    -   _O token expira em 5 minutos._

4. **Refresh**

   Para renovar o acesso sem refazer o login, utilize o `POST /api/v2/auth/refresh` enviando o seu `refreshToken` gerado junto com o token JWT. Cole o novo `accessToken` no campo `bearerAuth` novamente. Clique em **Authorize** e **Close**.

### Autenticação V1 (JSON Body)

Mantido para fins de versionamento conforme edital.

1.  **Login via Body:**

    -   Acesse `POST /api/v1/auth/login`.

    -   Envie o seguinte JSON:

        JSON

        ```
        {
          "username": "seletivo",
          "password": "admin"
        }
        
        ```

2.  **Ativação do Token:**

    -   Copie o `accessToken` gerado.

    -   Clique no botão **Authorize** (topo direito).

    -   Cole o token em `bearerAuth (http, Bearer)`. Clique em **Authorize** e **Close**.

3.  **Refresh V1:**

    -   Utilize o `POST /api/v1/auth/refresh` com o seu `refreshToken` para obter um novo token JWT Cole o novo `accessToken` no campo `bearerAuth` no botão Authorize(topo da pagina) no campo `bearerAuth` (http, Bearer) Value. em seguida Clique em **Authorize** e **Close**.

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

**JWT:** Access token com vida curta (ex.: 5 min); refresh token com vida maior (ex.: 30 min). Refresh tokens são armazenados em memória.

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

A **carga inicial (V2)** insere artistas (ex.: Serj Tankian, Mike Shinoda, Michel Teló, Guns N' Roses) e álbuns, permitindo testar listagens e filtros sem cadastro manual.

---

## Observabilidade

- **Actuator:** endpoints `health` e `info` expostos; health com detalhes (`show-details=always`).
- **Health customizado:** `MinioHealthIndicator` executa `listBuckets` no MinIO e inclui o status (UP/DOWN) no health agregado.
- **Monitor WebSocket:** página estática `/monitor.html` subscreve o tópico `/topic/novo-album` e exibe notificações de novos álbuns em tempo real; o acesso à página exige **Basic Auth** (tratado por `JwtAuthenticationEntryPoint`).  
  1 . Com a aplicação rodando, acesse: http://localhost:8080/monitor.html necessario login e senha:  

       Username: seletivo

       Password: admin  
 2 . O painel indicará o status ONLINE.

  3 . Ao realizar um POST de criação de álbum via Swagger ou Postman, a notificação aparecerá automaticamente na tela sem necessidade de refresh.

---

## Testes

Os testes unitários e de integração podem ser executados diretamente dentro do container da API para garantir paridade com o ambiente de execução.

### Rodando no Docker (Recomendado)

Execute a suíte de testes via Docker Exec:

```
docker exec -it artistas-api ./mvnw test
```
Rodando Localmente
Caso prefira rodar fora do container (requer Java 17 instalado):

```
./mvnw test
```
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

## 🚀 Futuras Implementações (Roadmap)

Roadmap de Escalabilidade e Novas Implementações::

1. **Persistência de Métricas com Prometheus & Grafana**:
    * Evoluir o monitoramento atual (`/monitor.html`) para uma solução de observabilidade completa, utilizando o **Micrometer** para exportar métricas para o Prometheus e visualizá-las em dashboards profissionais no Grafana.

2. **Autenticação de Dois Fatores (2FA/MFA)**:
    * Implementar uma camada extra de segurança no fluxo de login da **API V2**, integrando o envio de códigos temporários (TOTP) via e-mail ou aplicativos de autenticação (como Google Authenticator).

3. **Arquitetura de Mensageria com Redis Pub/Sub**:
    * Escalar o **WebSocket** para ambientes multi-container (Cluster Docker), utilizando o Redis como *Message Broker*. Isso garante que um evento enviado em uma instância da API seja replicado para todos os clientes conectados em outras instâncias.
