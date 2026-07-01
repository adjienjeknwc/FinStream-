# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd --gid 10001 appgroup && \
    useradd --uid 10001 --gid 10001 --shell /bin/sh --create-home appuser

RUN chown -R appuser:appgroup /app
USER 10001

COPY --from=build --chown=appuser:appgroup /app/target/loan-workflow-service-1.0.0.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
