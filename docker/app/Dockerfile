# =============================================================================
# ResearchEDF App (Spring Boot + React SPA) — Multi-stage Docker Build
# Stage 1: Build frontend (pnpm)
# Stage 2: Build WAR with Maven
# Stage 3: Deploy WAR to Tomcat 10.1 + JDK 21
# =============================================================================

# Stage 1a: Frontend build
# =============================================================================
FROM node:22-alpine AS frontend-build

WORKDIR /build/frontend

COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
COPY frontend/ ./

RUN corepack enable && CI=true pnpm install --frozen-lockfile && pnpm build

# Stage 1b: Maven build
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy parent POM first for dependency caching
COPY pom.xml ./
COPY research-edc-bom/pom.xml ./research-edc-bom/
COPY shared/pom.xml ./shared/
COPY web/pom.xml ./web/
COPY ws/pom.xml ./ws/
COPY app/pom.xml ./app/

# Download dependencies (cached layer unless POM changes)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -DskipTests -pl app -am --quiet || true

# Install proprietary ODM dependency (not in public repos)
COPY lib/openclinica-odm-2.2.jar lib/openclinica-odm-2.2.pom ./lib/
RUN --mount=type=cache,target=/root/.m2 mvn install:install-file -Dfile=lib/openclinica-odm-2.2.jar -DpomFile=lib/openclinica-odm-2.2.pom -q

# Copy source (shared + app modules needed)
COPY shared/ ./shared/
COPY app/ ./app/

# Copy frontend build output into app's static resources
COPY --from=frontend-build /build/frontend/dist/ ./app/src/main/resources/static/

# Build the WAR (skip tests)
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -pl app -am

# Stage 2: Runtime — Tomcat 10.1 + JDK 21
# =============================================================================
FROM tomcat:10.1-jdk21-temurin

LABEL org.opencontainers.image.title="ResearchEDF" \
      org.opencontainers.image.description="ResearchEDF App — Spring Boot + React SPA"

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR from build stage
COPY --from=build /build/app/target/OpenClinica-*.war /usr/local/tomcat/webapps/ROOT.war

# Create data and log directories
RUN mkdir -p /opt/researchedc/data /opt/researchedc/logs /opt/researchedc/config

# Copy entrypoint script
COPY docker/scripts/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Environment variables with defaults
# JVM tuning for containerized environments
# UseContainerSupport is default in Java 21; explicit for visibility
# MaxRAMPercentage=75 leaves headroom for native memory, metaspace, threads
ENV CATALINA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/researchedc/logs"

ENV RESEARCHEDC_DB_TYPE=postgres \
    RESEARCHEDC_DB_HOST=postgres \
    RESEARCHEDC_DB_PORT=5432 \
    RESEARCHEDC_FILE_PATH=/opt/researchedc/data \
    RESEARCHEDC_LOG_DIR=/opt/researchedc/logs \
    RESEARCHEDC_LOG_LEVEL=info \
    RESEARCHEDC_MAIL_HOST=mailhog \
    RESEARCHEDC_MAIL_PORT=1025 \
    RESEARCHEDC_MAIL_PROTOCOL=smtp \
    RESEARCHEDC_MAIL_AUTH=false \
    RESEARCHEDC_LDAP_ENABLED=false \
    RESEARCHEDC_MAX_INACTIVE_INTERVAL=3600 \
    TZ=UTC

VOLUME ["/opt/researchedc/data", "/opt/researchedc/logs", "/opt/researchedc/config"]

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["catalina.sh", "run"]
