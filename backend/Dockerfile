# ============================================================
# SmartBank Backend Dockerfile
# ============================================================

# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -q
# Build the application
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Run with minimal JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S smartbank && adduser -S smartbank -G smartbank
COPY --from=builder /app/target/smartbank-wallet-1.0.0.jar app.jar
RUN mkdir -p /app/uploads /app/logs && chown -R smartbank:smartbank /app
USER smartbank
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
