# ============================================================
# Mini LeetCode - Dockerfile
# Multi-stage build: compile with Maven, run with slim JRE
# ============================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached as a layer)
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copy source code and build
COPY src/ src/
RUN mvn package -DskipTests -B

# --- Stage 2: Run ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
