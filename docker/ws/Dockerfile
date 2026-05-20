# =============================================================================
# ResearchEDC Web Services — Multi-stage Docker Build
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
COPY web/pom.xml ./web/
COPY ws/pom.xml ./ws/
COPY app/pom.xml ./app/

# Download dependencies (cached layer unless POM changes)
RUN mvn dependency:go-offline -DskipTests -pl ws -am --quiet || true

# Install proprietary ODM dependency (not in public repos)
COPY lib/openclinica-odm-2.2.jar lib/openclinica-odm-2.2.pom ./lib/
RUN mvn install:install-file -Dfile=lib/openclinica-odm-2.2.jar -DpomFile=lib/openclinica-odm-2.2.pom -q

# Copy source (only needed modules)
COPY core/ ./core/
COPY ws/ ./ws/

# Build the WAR (skip tests)
RUN mvn clean package -DskipTests -pl ws -am

# Stage 2: Runtime — Tomcat 10.1 + JDK 21
# =============================================================================
FROM tomcat:10.1-jdk21-temurin

LABEL org.opencontainers.image.title="ResearchEDC" \
      org.opencontainers.image.description="ResearchEDC Web Services — SOAP Web Services (Tomcat 10.1 + JDK 21)"

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR from build stage
COPY --from=build /build/ws/target/OpenClinica-ws-*.war /usr/local/tomcat/webapps/ROOT.war

# Create data and log directories
RUN mkdir -p /opt/researchedc/data /opt/researchedc/logs /opt/researchedc/config

# Copy entrypoint script
COPY docker/scripts/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Environment variables with defaults
ENV RESEARCHEDC_DB_TYPE=postgres \
    RESEARCHEDC_DB_HOST=postgres \
    RESEARCHEDC_DB_PORT=5432 \
    RESEARCHEDC_DB_NAME=researchedc \
    RESEARCHEDC_DB_USER=researchedc \
    RESEARCHEDC_DB_PASS=researchedc \
    RESEARCHEDC_SYS_URL=http://localhost:8080/MainMenu \
    RESEARCHEDC_ADMIN_EMAIL=admin@example.com \
    RESEARCHEDC_FILE_PATH=/opt/researchedc/data \
    RESEARCHEDC_LOG_DIR=/opt/researchedc/logs \
    RESEARCHEDC_LOG_LEVEL=info \
    RESEARCHEDC_MAIL_HOST=mailhog \
    RESEARCHEDC_MAIL_PORT=1025 \
    RESEARCHEDC_MAIL_PROTOCOL=smtp \
    RESEARCHEDC_MAIL_AUTH=false \
    RESEARCHEDC_LDAP_ENABLED=false \
    TZ=UTC

VOLUME ["/opt/researchedc/data", "/opt/researchedc/logs", "/opt/researchedc/config"]

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["catalina.sh", "run"]
