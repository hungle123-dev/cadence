# syntax=docker/dockerfile:1
# ---- build ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Tests run in CI before images are built; the image build only packages.
RUN ./mvnw -B -q package -DskipTests && cp target/cadence-backend-*.jar app.jar

# ---- run ----
FROM eclipse-temurin:25-jre
RUN groupadd --system app && useradd --system --gid app --home /app app
WORKDIR /app
COPY --from=build /app/app.jar app.jar
COPY --chmod=755 docker/healthcheck.sh /usr/local/bin/healthcheck
USER app
EXPOSE 8080
# start-period: Spring Boot + Flyway need some seconds before the first check counts
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 CMD ["healthcheck"]
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
