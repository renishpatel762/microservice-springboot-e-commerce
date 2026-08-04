# ===================================================================================
# MULTI-STAGE DOCKERFILE FOR ORDER SERVICE
# ===================================================================================
# --- Stage 1: Build & Package ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy pom.xml and wrapper files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY order-service order-service
COPY product-service/pom.xml product-service/pom.xml

# Package order-service JAR
RUN ./mvnw clean package -pl order-service -DskipTests

# --- Stage 2: Production JRE Runtime ---
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Run as non-root user for security hardiness
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/order-service/target/order-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]
