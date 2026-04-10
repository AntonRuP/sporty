# sporty

Spring Boot 3 / Java 21 service that accepts event live-status updates, polls a mocked score provider for live events, and publishes score updates to Kafka.

## Stack

- Java 21
- Gradle wrapper
- Spring Boot Web, Validation, and Actuator
- Spring Kafka
- Spring Retry for Kafka publishing retries
- Lombok for logger and constructor boilerplate
- Swagger/OpenAPI via `springdoc`
- Embedded Kafka for integration tests

## How It Works

- `POST /events/status` accepts an `eventId` and a status of `LIVE` or `NOT_LIVE`.
- Live event state is kept in memory.
- Each live event gets one scheduled polling task.
- Polling uses `ExternalScoreClient`, which is currently a mocked in-process provider returning score `2:1` for the requested event id.
- Each score response is transformed into `ScoreUpdateMessage` and published to the configured Kafka topic.
- Marking an event `NOT_LIVE` cancels its polling task.

## Local Prerequisites

- Java 21 available locally
- Docker Desktop or compatible Docker engine
- PowerShell, cmd, Git Bash, or another shell that can run the Gradle wrapper

## Start Locally

Start Kafka:

```bash
docker compose up -d
```

Run the service with the Gradle wrapper:

```bash
./gradlew bootRun
```

On Windows PowerShell, use:

```powershell
.\gradlew.bat bootRun
```

The service starts on `http://localhost:8080`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

## Configuration

Defaults are defined in `src/main/resources/application.yml`.

| Variable | Default | Purpose |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `POLLING_INTERVAL_MS` | `10000` | Poll interval for live events |
| `SPORTY_KAFKA_TOPIC` | `live-score-updates` | Kafka topic for score updates |

Kafka auto-topic creation is enabled in `docker-compose.yml`, so the default topic is created on first publish.

## Example Request

Mark event `1234` as live:

```bash
curl -X POST http://localhost:8080/events/status \
  -H "Content-Type: application/json" \
  -d '{"eventId":"1234","status":"LIVE"}'
```

Stop polling for the same event:

```bash
curl -X POST http://localhost:8080/events/status \
  -H "Content-Type: application/json" \
  -d '{"eventId":"1234","status":"NOT_LIVE"}'
```

Response example:

```json
{
  "eventId": "1234",
  "status": "LIVE",
  "pollingActive": true
}
```

## Observe Kafka Messages

With Docker Compose running, consume the default topic:

```bash
docker compose exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic live-score-updates \
  --from-beginning
```

The produced JSON contains the event id and mocked score.


## Design Notes

- `EventLifecycleService` owns event state and polling task lifecycle.
- `EventPollingJob` calls the mocked score client and publishes the transformed message.
- `ScorePublisher` wraps Kafka sends with Spring Retry and logs successful publication.
- `GlobalExceptionHandler` returns structured errors for validation, malformed JSON, and publish failures.
- `InfrastructureConfig` defines the scheduler, Kafka producer factory, Kafka template, and retry template.
