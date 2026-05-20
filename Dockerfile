# syntax=docker/dockerfile:1.7
# Stage 1 — dependencies (cache Maven)
FROM eclipse-temurin:21-jdk-alpine AS dependencies
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Stage 2 — build
FROM dependencies AS builder
COPY src ./src
RUN ./mvnw clean package -DskipTests -B \
    && java -Djarmode=layertools -jar target/*.jar extract

# Stage 3 — runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
# hadolint ignore=DL3018
RUN apk add --no-cache curl tini \
    && addgroup -g 1000 lumiris \
    && adduser -D -u 1000 -G lumiris lumiris
USER lumiris
WORKDIR /app

COPY --from=builder --chown=lumiris:lumiris /build/dependencies/ ./
COPY --from=builder --chown=lumiris:lumiris /build/spring-boot-loader/ ./
COPY --from=builder --chown=lumiris:lumiris /build/snapshot-dependencies/ ./
COPY --from=builder --chown=lumiris:lumiris /build/application/ ./

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxMetaspaceSize=128m"
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=60s \
  CMD curl -fs http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["tini", "--", "java", "org.springframework.boot.loader.launch.JarLauncher"]
