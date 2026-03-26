# Device Service - Spring Boot REST API

Device Service is a REST API for managing devices and their lifecycle states.
The service allows creating devices, retrieving devices by ID, searching devices with filters, and managing device state.
The project is built using Hexagonal Architecture (Ports & Adapters) to ensure clear separation between domain logic and infrastructure.
## Table of Contents

- [Project Overview](#-project-overview)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Features](#-features)
- [Local Development](#-local-development)
- [Testing](#-testing)
- [API Endpoints](#-api-endpoints)
- [Database](#-database)
- [Docker](#-docker)
- [Project Structure](#-project-structure)

---

## Project Overview

**Device Service** is a Spring Boot 4.0.4 application that provides RESTful APIs for device management. It demonstrates industry best practices including:

-  **Hexagonal Architecture** (Ports & Adapters pattern)
-  **Domain-Driven Design** principles
-  **Clean Code** with SOLID principles
-  **Database Migrations** with Flyway
-  **Containerization** with Docker & Docker Compose
-  **API Documentation** with OpenAPI/Swagger
-  **Input Validation** at multiple layers


---

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 4.0.4 |
| **Build Tool** | Maven | 3.9+ |
| **ORM** | Spring Data JPA | Latest |
| **Database** | PostgreSQL | 17 |
| **Migrations** | Flyway | Latest |
| **Testing** | JUnit 5, Mockito, AssertJ | Latest |
| **Containers** | Docker, Docker Compose | Latest |
| **API Docs** | SpringDoc OpenAPI | 3.0.2 |
| **JSON** | Jackson | Latest |

---

## Architecture

### Hexagonal Architecture Pattern

The application follows the Hexagonal (Ports & Adapters) architecture pattern:

```
adapter.in.rest         -> REST controllers
application             -> Use cases / services
domain                  -> Domain models and business rules
adapter.out.persistence -> JPA repositories and persistence adapters
configuration           -> Spring configuration
```

**Benefits:**
- Business logic is independent of frameworks
- Easy to test with mocks
- Flexible to swap implementations
- Clear separation of concerns

---

## Running Locally

### Prerequisites

```bash
# Required
- Java 21 or higher
- Maven 3.9 or higher
- Git
- Docker
- Docker Compose
```

### Setup & Run

**1. Clone the repository**
```bash
git clone <repository-url>
cd device-manager
```

**2. Build the project**
```bash
make clean build
# or
./mvnw clean package
```

**3. Run with Local Profile**
```bash
make run-local
# or
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

The application will start on `http://localhost:8080`

**4. Access APIs & Documentation**

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## Docker

### Run with Docker Compose

**Start all services**
```bash
make run-docker
# or
docker-compose up -d
```

**Stop services**
```bash
make stop-docker
# or
docker-compose down
```

### Environment Variables

Create a `.env` file:
```ini
POSTGRES_DB=devicedb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/devicedb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
SERVER_PORT=8080
TZ=UTC
```

### Dockerfile

Multi-stage build for optimized image:
1. **Build Stage**: Maven builds with Java 21
2. **Runtime Stage**: Eclipse Temurin JRE runs the app
3. **Exposed Port**: 8080

---

## Features

### Device Management APIs

```
GET    /api/v1/devices              # Get all devices
GET    /api/v1/devices/{id}         # Get device by ID
POST   /api/v1/devices              # Create new device
PATCH  /api/v1/devices/{id}         # Partially update device
DELETE /api/v1/devices/{id}         # Delete device
GET    /api/v1/devices/search       # Search with filters
```

### Advanced Filtering

- **Filter by Name**: Case-insensitive exact matching
- **Filter by Brand**: Case-insensitive exact matching
- **Filter by State**: AVAILABLE, IN_USE, INACTIVE
- **Multi-Criteria**: Combine all filters
- **Auto-Trimming**: Removes leading/trailing whitespace

### Domain Model

```java
public record Device(
    UUID id,                    // Unique identifier
    String name,                // Device name (non-null)
    String brand,               // Device brand (non-null)
    DeviceState state,          // Current state
    Instant creationTime,       // Auto-generated timestamp
    Instant updateTime          // Auto-updated timestamp
)

public enum DeviceState {
    AVAILABLE,  // Device is available for use
    IN_USE,     // Device is currently in use
    INACTIVE    // Device is inactive/retired
}
```

### Data Validation

- **Request Level**: @Valid annotations on DTOs
- **Business Level**: Custom validators in service
- **Domain Level**: Record constructors with checks
- **Detailed Errors**: Violation messages returned to client

### Database Features

- **Automatic Versioning**: Flyway manages schema versions
- **Timestamps**: creation_time & update_time tracked automatically
- **Indexes**: Optimized queries on brand and state columns
- **Triggers**: PostgreSQL trigger updates update_time on modifications
- **Connection Pooling**: Hikari with optimized settings

---

### Running Tests

```bash
# Run all tests
make test
./mvnw test

# Run specific test class
./mvnw test -Dtest=DeviceServiceTest

# Run integration tests only
./mvnw test -Dtest=*IntegrationTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Infrastructure

- **Testcontainers**: Real PostgreSQL in Docker
- **Flyway**: Migrations run in test environment
- **Mock & Stubs**: Strategic mocking for unit tests
- **Assertions**: AssertJ for fluent assertions
- **Isolation**: Each test is independent

---

## API Endpoints

### Get All Devices
```bash
curl -X GET http://localhost:8080/api/devices
```

**Response:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "iPhone 15",
    "brand": "Apple",
    "state": "AVAILABLE",
    "creationTime": "2026-03-26T10:00:00Z",
    "updateTime": "2026-03-26T10:00:00Z"
  }
]
```

### Get Device by ID
```bash
curl -X GET http://localhost:8080/api/devices/{id}
```

### Create Device
```bash
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "brand": "Apple",
    "state": "AVAILABLE"
  }'
```

### Update Device (PATCH)
```bash
curl -X PATCH http://localhost:8080/api/devices/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "state": "IN_USE"
  }'
```

### Delete Device
```bash
curl -X DELETE http://localhost:8080/api/devices/{id}
```

### Search with Filters
```bash
curl -X GET "http://localhost:8080/api/devices/search?name=iPhone&brand=Apple&state=AVAILABLE"
```

---

## Database

### PostgreSQL 17

**Connection Details (Local)**
- Host: localhost
- Port: 5432
- Database: devicedb
- User: postgres
- Password: postgres

### Migrations

Flyway automatically runs migrations on startup:

1. **V1__initial_create_device_table.sql**: Initial schema
   - Creates `devices` table
   - Creates `device_state` enum type
   - Creates indexes on brand & state
   - Creates trigger for update_time

2. **V2__update_device_states.sql**: State updates
3. **V3__update_device_states_to_varchar.sql**: State type migration

**Repeatable Migrations:**
- **R__test_data.sql**: Seeds test data (12 devices)

---


## Configuration

### Application Profiles

**Local** (`application-local.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/devicedb
spring.datasource.username=postgres
spring.datasource.password=password
```

**Docker** (`application-docker.properties`)
```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/devicedb
spring.datasource.username=postgres
spring.datasource.password=password
```

**Test** (`application-test.properties`)
```properties
spring.flyway.enabled=true
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.idle-timeout=10000
```

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

# Application
SERVER_PORT
SPRING_PROFILES_ACTIVE

# Logging
LOGGING_LEVEL_COM_SERCAN_DEVICE_SERVICE
```

---

## Makefile Commands

```bash
make help              # Show available commands
make clean             # Clean build artifacts
make build             # Build the application
make test              # Run all tests
make run-local         # Run locally with local profile
make run-docker        # Start Docker containers
make stop-docker       # Stop Docker containers
```

---


## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)
- [Flyway Migrations](https://flywaydb.org/)
- [Testcontainers](https://www.testcontainers.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

