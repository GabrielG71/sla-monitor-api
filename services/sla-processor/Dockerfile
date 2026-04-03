# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copia o parent pom (raiz)
COPY pom.xml .

# Copia o pom do serviço
COPY services/sla-processor/pom.xml services/sla-processor/pom.xml

WORKDIR /build/services/sla-processor

# Cache de dependências
RUN mvn dependency:go-offline -q

# Copia o código
COPY services/sla-processor/src ./src

# Build
RUN mvn package -DskipTests -q

# ── Runtime ───────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S sla && adduser -S sla -G sla
USER sla

COPY --from=build /build/services/sla-processor/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]