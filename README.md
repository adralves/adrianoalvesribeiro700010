# artistas-api

API REST de **Artistas** e **Álbuns**, com relacionamento **N:N** (tabela de junção `artista_album`), usando **Spring Boot + JPA** e **PostgreSQL**. O schema do banco é criado/validado via **Flyway**.

## Requisitos

- **Java 17**
- **Docker Desktop** (recomendado para subir o PostgreSQL)
- Portas livres:
  - **5432** (PostgreSQL)
  - **8080** (API)

## Como rodar (recomendado)

1) Suba o PostgreSQL:

```bash
docker compose up -d
```

2) Suba a API (Windows / PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Como rodar via JAR (java -jar)

> No `pom.xml` o boot jar é gerado como `*-exec.jar` (evita falhas de rename no Windows).

```powershell
.\mvnw.cmd -DskipTests package
java -jar .\target\artistas-api-0.0.1-SNAPSHOT-exec.jar
```

## Configuração do banco
Usuario: postgres  
Senha: postgres  
Nome do Banco de Dados: artistasdb

Em `src/main/resources/application.properties`:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/artistasdb`
- `spring.datasource.username=postgres`
- `spring.datasource.password=postgres`

As migrations ficam em `src/main/resources/db/migration`.

## Endpoints

- **Criar artista**: `POST /api/v1/artistas`
- **Listar artistas (paginado + filtro por nome)**: `GET /api/v1/artistas?pagina=0&tamanho=10&ordem=asc&nome=ana`
- **Criar álbum para um artista**: `POST /api/v1/albuns/artista/{artistaId}`
- **Listar álbuns**: `GET /api/v1/albuns`
- **Healthcheck**: `GET /actuator/health`

### Exemplos rápidos (PowerShell)

```powershell
# Criar artista
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/artistas" `
  -ContentType "application/json" -Body '{"nome":"Ana"}'

# Listar artistas
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/artistas?pagina=0&tamanho=10&ordem=asc"

# Criar álbum para artista 1
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/albuns/artista/1" `
  -ContentType "application/json" -Body '{"nome":"Meu Álbum"}'
```

## Segurança

O projeto está configurado para **permitir todas as requisições** (sem autenticação) em `SecurityConfig`.