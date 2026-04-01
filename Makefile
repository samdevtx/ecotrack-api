.PHONY: dev test build lint clean

dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up

stack:
	docker compose up -d

test:
	./mvnw verify

build:
	./mvnw package -DskipTests

lint:
	./mvnw checkstyle:check spotbugs:check

clean:
	./mvnw clean
	docker compose down -v
