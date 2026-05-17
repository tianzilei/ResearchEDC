# =============================================================================
# OpenClinica Web Services — Multi-stage Docker Build
# Stage 1: Build WAR with Maven
# Stage 2: Deploy WAR to Tomcat 10.1 + JDK 21
# =============================================================================

# Stage 1: Build
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy parent POM first for dependency caching
COPY pom.xml ./
COPY core/pom.xml ./core/
COPY ws/pom.xml ./ws/

# Download dependencies (cached layer unless POM changes)
RUN mvn dependency:go-offline -DskipTests -pl ws -am --quiet || true

# Copy source
COPY core/ ./core/
COPY ws/ ./ws/

# Build the WAR (skip tests)
RUN mvn clean package -DskipTests -pl ws -am

# Stage 2: Runtime — Tomcat 10.1 + JDK 21
# =============================================================================
FROM tomcat:10.1-jdk21-temurin

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR from build stage
COPY --from=build /build/ws/target/OpenClinica-ws-*.war /usr/local/tomcat/webapps/ROOT.war

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
    OC_SYS_URL=http://localhost:8080/MainMenu \
    OC_ADMIN_EMAIL=admin@example.com \
    OC_FILE_PATH=/opt/openclinica/data \
    OC_LOG_DIR=/opt/openclinica/logs \
    OC_LOG_LEVEL=info \
    OC_MAIL_HOST=mailhog \
    OC_MAIL_PORT=1025 \
    OC_MAIL_PROTOCOL=smtp \
    OC_MAIL_AUTH=false \
    OC_LDAP_ENABLED=false \
    TZ=UTC

VOLUME ["/opt/openclinica/data", "/opt/openclinica/logs", "/opt/openclinica/config"]

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["catalina.sh", "run"]
