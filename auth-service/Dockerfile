# ===================================================================================
# MULTI-STAGE DOCKERFILE FOR AUTH SERVICE
# ===================================================================================

# --- Stage 1: Build & Package ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy root pom.xml and wrapper files for caching dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY auth-service auth-service
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml

# Package auth-service JAR
RUN ./mvnw clean package -pl auth-service -DskipTests

# --- Stage 2: Production JRE Runtime ---
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Run as non-root user for security hardiness
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy repackaged fat JAR from builder stage
COPY --from=builder /app/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]
