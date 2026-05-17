# =============================================================================
# OpenClinica App (Spring Boot + React SPA) — Multi-stage Docker Build
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

RUN corepack enable && pnpm install --frozen-lockfile && pnpm build

# Stage 1b: Maven build
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy parent POM first for dependency caching
COPY pom.xml ./
COPY core/pom.xml ./core/
COPY web/pom.xml ./web/
COPY ws/pom.xml ./ws/
COPY app/pom.xml ./app/

# Download dependencies (cached layer unless POM changes)
RUN mvn dependency:go-offline -DskipTests -pl app -am --quiet || true

# Copy source (core + app modules needed)
COPY core/ ./core/
COPY app/ ./app/

# Copy frontend build output into app's static resources
COPY --from=frontend-build /build/frontend/app/src/main/resources/static/ ./app/src/main/resources/static/

# Build the WAR (skip tests)
RUN mvn clean package -DskipTests -pl app -am

# Stage 2: Runtime — Tomcat 10.1 + JDK 21
# =============================================================================
FROM tomcat:10.1-jdk21-temurin

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR from build stage
COPY --from=build /build/app/target/OpenClinica-*.war /usr/local/tomcat/webapps/ROOT.war

# Create data and log directories
RUN mkdir -p /opt/openclinica/data /opt/openclinica/logs /opt/openclinica/config

# Copy entrypoint script
COPY docker/scripts/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Environment variables with defaults
ENV OC_DB_TYPE=postgres \
    OC_DB_HOST=postgres \
    OC_DB_PORT=5432 \
    OC_DB_NAME=openclinica \
    OC_DB_USER=openclinica \
    OC_DB_PASS=openclinica \
    OC_SYS_URL=http://localhost:8080/app/dashboard \
    OC_ADMIN_EMAIL=admin@example.com \
    OC_FILE_PATH=/opt/openclinica/data \
    OC_LOG_DIR=/opt/openclinica/logs \
    OC_LOG_LEVEL=info \
    OC_MAIL_HOST=mailhog \
    OC_MAIL_PORT=1025 \
    OC_MAIL_PROTOCOL=smtp \
    OC_MAIL_AUTH=false \
    OC_LDAP_ENABLED=false \
    OC_MAX_INACTIVE_INTERVAL=3600 \
    TZ=UTC

VOLUME ["/opt/openclinica/data", "/opt/openclinica/logs", "/opt/openclinica/config"]

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["catalina.sh", "run"]
