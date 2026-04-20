# Stage 1: Build the application using Maven
FROM eclipse-temurin:17-jdk-alpine@sha256:0b31cde347425a20347179c41dc38fa2b958a6adbb6f967ecce25c841fbf474c AS builder

# Metadata labels
LABEL maintainer="samdevtx"
LABEL description="EcoTrack - Mobilidade Sustentável Application"
LABEL version="1.0.0"

WORKDIR /app

# Install necessary packages and create cache directory
RUN apk add --no-cache curl && \
    mkdir -p /root/.m2

# Copy Maven wrapper and pom.xml to leverage Docker cache for dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make mvnw executable and download dependencies
RUN chmod +x ./mvnw && \
    ./mvnw dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Package the application with optimizations
RUN ./mvnw clean package -DskipTests -B -Dspring.profiles.active=docker && \
    # Extract JAR layers for better caching
    java -Djarmode=layertools -jar target/mobilidade-sustentavel-*.jar extract

# Stage 2: Create the lightweight runtime image
FROM eclipse-temurin:17-jre-alpine@sha256:9c68a49228fd2684ab0e2d36b3405ab9c80974fa70bacac14fa24d883e76c0d7

# Metadata labels
LABEL maintainer="samdevtx"
LABEL description="EcoTrack - Mobilidade Sustentável Runtime"
LABEL version="1.0.0"

# Install security updates and necessary packages
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        dumb-init \
        tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=America/Sao_Paulo

WORKDIR /app

# Create a non-root user and group with specific UID/GID
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy application layers from builder stage for better caching
COPY --from=builder --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/application/ ./

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app/logs

# Switch to the non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose the port the application runs on
EXPOSE 8080

# JVM optimization arguments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=docker"

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Set the command to run the application with optimized JVM settings
CMD ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]