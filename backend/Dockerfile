# ---- Stage 1: build the jar (JDK + Maven wrapper live only here) ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Dependencies first: this layer is rebuilt only when pom.xml changes.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

# Tests run in CI (they need Testcontainers), not inside docker build.
COPY src src
RUN ./mvnw -B -q -DskipTests package

# Split the jar into layers: libraries rarely change, our code changes often.
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---- Stage 2: run (JRE only, non-root) ----
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

# Least to most volatile, so a code-only change re-pushes one small layer.
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
