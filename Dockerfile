# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

LABEL maintainer="samdevtx"

WORKDIR /app

RUN apk add --no-cache curl

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B && \
    mv target/mobilidade-sustentavel-*.jar target/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="samdevtx"

RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl dumb-init tzdata && \
    rm -rf /var/cache/apk/*

ENV TZ=America/Sao_Paulo

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /app/target/app.jar app.jar

RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
