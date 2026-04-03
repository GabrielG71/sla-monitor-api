# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copia o pom pai (raiz do projeto)
COPY pom.xml .

# Copia o pom do serviço
COPY services/ingestor-service/pom.xml services/ingestor-service/pom.xml

# Vai pro módulo
WORKDIR /build/services/ingestor-service

# Cache de dependências
RUN mvn dependency:go-offline -q

# Copia o código
COPY services/ingestor-service/src ./src

# Build
RUN mvn package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S sla && adduser -S sla -G sla
USER sla

COPY --from=build /build/services/ingestor-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]