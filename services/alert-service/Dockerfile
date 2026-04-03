# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copia o pom pai (raiz do projeto)
COPY pom.xml .

# Copia o pom do serviço
COPY services/alert-service/pom.xml services/alert-service/pom.xml

# Entra no módulo
WORKDIR /build/services/alert-service

# Baixa dependências (cache layer)
RUN mvn dependency:go-offline -q

# Copia o código fonte
COPY services/alert-service/src ./src

# Build da aplicação
RUN mvn package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Segurança (não roda como root)
RUN addgroup -S sla && adduser -S sla -G sla
USER sla

# Copia o jar gerado
COPY --from=build /build/services/alert-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]