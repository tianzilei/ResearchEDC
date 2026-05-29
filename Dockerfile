FROM node:22-alpine AS frontend
WORKDIR /build/frontend
RUN corepack enable && corepack prepare pnpm@11.1.2 --activate
COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --ignore-scripts && pnpm rebuild esbuild
COPY frontend/ .
RUN pnpm build

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /build

COPY lib/ lib/
RUN mvn install:install-file \
    -Dfile=lib/openclinica-odm-2.2.jar \
    -DpomFile=lib/openclinica-odm-2.2.pom \
    -q

COPY pom.xml ./
COPY research-edc-bom/ research-edc-bom/
COPY shared/ shared/
COPY web/ web/
COPY ws/ ws/
COPY app/ app/
RUN rm -rf app/src/main/resources/static/
COPY --from=frontend /build/frontend/dist/ app/src/main/resources/static/
RUN mvn package -pl app -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache unzip zip
WORKDIR /app
COPY --from=backend /build/app/target/ResearchEDC.war ./app.war
RUN unzip -o app.war WEB-INF/classes/datainfo.properties && \
    sed -i 's/dbHost=localhost/dbHost=postgres/' WEB-INF/classes/datainfo.properties && \
    sed -i 's/dbPort=5433/dbPort=5432/' WEB-INF/classes/datainfo.properties && \
    zip -u app.war WEB-INF/classes/datainfo.properties
EXPOSE 8080
CMD ["java", "-jar", "app.war", "--spring.profiles.active=dev"]
