.DEFAULT_GOAL := help
.PHONY: help clean build test run-local run-docker stop-docker logs app-logs db-logs postgres-up postgres-down

MVN=./mvnw

help:
	@echo "Available commands:"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make build         - Build the application"
	@echo "  make test          - Run tests"
	@echo "  make run-local     - Run application locally with local profile"
	@echo "  make run-docker    - Build and start all Docker containers"
	@echo "  make stop-docker   - Stop all Docker containers"

clean:
	$(MVN) clean

build:
	$(MVN) clean package

test:
	$(MVN) test

run-local:
	SPRING_PROFILES_ACTIVE=local $(MVN) spring-boot:run

run-docker:
	docker compose up --build

stop-docker:
	docker compose down
