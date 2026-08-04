# ===================================================================================
# MULTI-STAGE DOCKERFILE FOR PRODUCT SERVICE
# ===================================================================================
# Why Multi-stage Builds exist:
# - Stage 1 (Builder): Uses full JDK image with Maven toolchain to compile code and package fat JAR.
# - Stage 2 (Runner): Uses minimal JRE runtime image, copying ONLY the compiled fat JAR.
# Reduces container image size from ~800MB to ~200MB and eliminates build security vulnerabilities.
# ===================================================================================

# --- Stage 1: Build & Package ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy root pom.xml and wrapper files for caching dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY product-service product-service
COPY order-service/pom.xml order-service/pom.xml

# Package product-service JAR
RUN ./mvnw clean package -pl product-service -DskipTests

# --- Stage 2: Production JRE Runtime ---
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Run as non-root user for security hardiness
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy repackaged fat JAR from builder stage
COPY --from=builder /app/product-service/target/product-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]
