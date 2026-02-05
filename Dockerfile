# ===== ETAPA 1 - BUILD =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ===== ETAPA 2 - EXECUÇÃO =====
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiamos o JAR para execução
COPY --from=build /app/target/*exec.jar app.jar

# COPIAMOS o código e o wrapper para permitir testes dentro do container
COPY --from=build /app/src ./src
COPY --from=build /app/pom.xml ./pom.xml
COPY --from=build /app/mvnw ./mvnw
COPY --from=build /app/.mvn ./.mvn

# Garantir permissão de execução no wrapper
RUN chmod +x mvnw

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]