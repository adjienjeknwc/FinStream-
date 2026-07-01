# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies by copying only the pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the packaged jar file
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Run Stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root system group and user for security compliance
RUN groupadd --gid 10001 appgroup && \
    useradd --uid 10001 --gid 10001 --shell /bin/sh --create-home appuser

# Set proper ownership for the /app directory
RUN chown -R appuser:appgroup /app

# Switch to the non-root user
USER 10001

# Copy the built artifact from the build stage
COPY --from=build --chown=appuser:appgroup /app/target/account-management-service-1.0.0.jar app.jar

# Expose standard container port
EXPOSE 8080

# Configure JVM tuning and execute the Spring Boot application
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
