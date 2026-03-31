# Development Workflow

## Start Infrastructure (local dev)

```bash
cd infra
docker compose up -d zookeeper kafka kafka-ui postgres redis
```

## Create Kafka Topics (first time)

```bash
bash infra/kafka/init-topics.sh
```

Or manually:

```bash
docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic raw-checks --partitions 6 --replication-factor 1

docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic sla-ok --partitions 3 --replication-factor 1

docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic sla-violations --partitions 3 --replication-factor 1

docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic raw-checks.DLT --partitions 1 --replication-factor 1
```

## Run a Service Locally (outside Docker)

```bash
cd services/ingestor-service
./mvnw spring-boot:run
```

## Run All Services via Compose

```bash
cd infra
docker compose up --build
```

## Service Ports

| Service        | Port  |
|---------------|-------|
| ingestor-service | 8081 |
| sla-processor    | 8082 |
| alert-service    | 8083 |
| frontend         | 3000 |
| Kafka UI         | 8090 |
| PostgreSQL       | 5432 |
| Redis            | 6379 |
| Kafka            | 9092 |

## After Completing Any Task

1. **Commit semantically** (no description body):
   ```
   git add <relevant files>
   git commit -m "type(scope): short message"
   ```
2. **Update memory/** — tick off roadmap items, add new known issues, update workflow if needed.
