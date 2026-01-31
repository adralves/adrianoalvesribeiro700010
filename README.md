# artistas-api

API REST de **Artistas** e **Álbuns**, com relacionamento **N:N** (tabela de junção `artista_album`), usando **Spring Boot + JPA** e **PostgreSQL**. O schema do banco é criado/validado via **Flyway**.

Este projeto utiliza Docker + Docker Compose para subir o banco de dados PostgreSQL e o MinIO de forma padronizada e reprodutível.

As imagens possuem versões fixas para garantir que o ambiente seja sempre o mesmo, independentemente da máquina.

## Requisitos

- **Java 17**
- **Docker** 
- **Docker Compose (ou Docker Desktop)**
- Portas livres:
  - **5432** (PostgreSQL)
  - **8080** (API)

## Como rodar 

1) Para garantir que tudo suba do zero:

```bash
docker compose down -v --remove-orphans
```

2) Subir os serviços:

```powershell
docker compose up -d
```

A API sobe em `http://localhost:8080`.

3) Verificar se está rodando

```powershell
docker ps
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

MinIO Console Web

http://localhost:9001

Login:  
Usuário: minioadmin  
Senha: minioadmin

---
## ------ATENÇÃO PRECISA REVISAR ESSA PARTE DA DOCUMENTACAO--------- 

## Endpoints

- **Criar artista**: `POST /api/v1/artistas`
- **Listar artistas (paginado + filtro por nome)**: `GET /api/v1/artistas?pagina=0&tamanho=10&ordem=asc&nome=ana`
- **Criar álbum para um artista**: `POST /api/v1/albuns/artista/{artistaId}`
- **Listar álbuns**: `GET /api/v1/albuns`


## Segurança

O projeto está configurado para **permitir todas as requisições** (sem autenticação) em `SecurityConfig`.