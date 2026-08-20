# Production-Ready Enterprise Spring Boot Microservices Architecture

A real-world, enterprise-grade E-Commerce backend architecture built with **Java 21** and **Spring Boot 3.3.x**, featuring dedicated **JWT Authentication & Authorization (`auth-service`)**, clean package organization, immutable DTO records, MapStruct compile-time mappers, Flyway database migrations, WebClient inter-service communication with correlation ID & Bearer token propagation, MDC structured logging, and Docker containerization.

---

## 🏛️ System Architecture

```
                                      +---------------------------+
                                      |  API Client / Swagger UI  |
                                      +-------------+-------------+
                                                    |
                   +--------------------------------+--------------------------------+
                   | POST /signup, /login           | HTTP + Bearer Token            | HTTP + Bearer Token
                   v                                v                                v
       +-----------------------+        +-----------------------+        +-----------------------+
       |     Auth Service      |        |    Product Service    | <---   |     Order Service     |
       |       Port 8083       |        |      Port 8081        |  Web   |       Port 8082       |
       +-----------+-----------+        +-----------+-----------+ Client +-----------+-----------+
                   |                                |  (Stock Reduction)             |
                   v                                v                                v
       +-----------------------+        +-----------------------+        +-----------------------+
       |   auth-db (Postgres)  |        |  product-db (Postgres)|        |   order-db (Postgres) |
       |       Port 5434       |        |       Port 5432       |        |       Port 5433       |
       +-----------------------+        +-----------------------+        +-----------------------+
```

---

## 🚀 Tech Stack & Design Decisions

| Category | Technology | Rationale & Architectural Choice |
|---|---|---|
| **Runtime** | Java 21 (LTS) | Modern Java features: Records, Sealed types, Pattern Matching, Virtual Threads readiness. |
| **Framework** | Spring Boot 3.3.5 | Production baseline implementing Jakarta EE 10 specifications. |
| **Security & Auth** | Spring Security + JJWT (0.12.6) | Dedicated Auth service issuing HMAC-SHA256 JWT tokens. Stateless resource authorization filters on all microservices. |
| **Persistence** | Spring Data JPA / Hibernate | Repository pattern abstraction with JPA dynamic specifications. Strictly isolated PostgreSQL DB schemas per service. |
| **Database Migrations**| Flyway | Version-controlled SQL scripts (`db/migration/V1__...sql`). Production `ddl-auto=validate`. |
| **Mapping Layer** | MapStruct 1.6.3 | Zero-reflection, compile-time type-safe object mapping between Entities and Records. |
| **REST Client** | Spring WebClient | Non-blocking reactive REST client with configured connection/read timeouts, MDC Correlation ID, and Bearer token header forwarding. |
| **API Documentation** | Springdoc OpenAPI 3.0 | Interactive OpenAPI & Swagger UI (`/swagger-ui.html`) with Bearer Auth Security Scheme integration. |
| **Observability** | SLF4J + MDC + Servlet Filters | Automatic UUID Correlation ID generation (`X-Correlation-ID`) injected into MDC logs and forwarded downstream. |
| **Containerization** | Docker & Docker Compose | Multi-stage Dockerfiles utilizing Eclipse Temurin JRE base images with non-root security execution. |

---

## 📚 Key Architectural Patterns & Educational Guide

### 1. Dedicated Authentication & User Microservice (`auth-service`)
- **Why it exists:** Isolates user credentials, BCrypt password hashing, and token issuance from domain microservices.
- **Stateless Authorization:** `auth-service` issues signed JWT tokens. `product-service` and `order-service` validate tokens statelessly using a shared secret key without database lookups on every request.

### 2. Header & Bearer Token Propagation in Inter-Service Communication
- **Why it exists:** When `order-service` calls `product-service` via WebClient to validate product stock and reduce inventory, it forwards both the client's `X-Correlation-ID` and `Authorization: Bearer <token>` header downstream.

### 3. Immutable DTOs via Java Records (`record`)
- Enforces thread-safe immutability at the syntax level. Prevents N+1 queries, circular serialization loops, and security leaks caused by returning raw JPA Entities.

### 4. Flyway Database Migrations
- Guaranteed version-controlled database schema management (`V1__create_users_table.sql`, `V1__create_products_table.sql`, `V1__create_orders_table.sql`).

---

## 📁 Repository Structure

```
microservice-springboot-e-commerce/
├── pom.xml                                  # Parent POM managing dependencies & JJWT versions
├── mvnw & mvnw.cmd                          # Self-contained Maven Wrappers
├── docker-compose.yml                       # Docker Compose setup for services & Postgres DBs
├── README.md                                # System Documentation
├── auth-service/                            # Authentication & User Management Service (Port 8083)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/ecommerce/auth/
│       │   ├── AuthServiceApplication.java
│       │   ├── config/                      # SecurityConfig & OpenApiConfig
│       │   ├── controller/                  # AuthController (/signup, /login, /me)
│       │   ├── dto/                         # SignupRequest, LoginRequest, AuthResponse records
│       │   ├── entity/                      # User entity
│       │   ├── exception/                   # User exceptions & GlobalExceptionHandler
│       │   ├── filter/                      # CorrelationId & RequestLogging filters
│       │   ├── repository/                  # UserRepository
│       │   ├── security/                    # JwtService & JwtAuthenticationFilter
│       │   └── service/                     # AuthService logic & BCrypt encoding
│       └── main/resources/
│           ├── application.yml              # Profile configs
│           └── db/migration/                # Flyway V1__create_users_table.sql
├── product-service/                         # Product Catalog Service (Port 8081)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/java/com/ecommerce/product/
│           ├── config/                      # SecurityConfig & OpenApiConfig
│           └── security/                    # JwtService & JwtAuthenticationFilter
└── order-service/                           # Order Management Service (Port 8082)
    ├── Dockerfile
    ├── pom.xml
    └── src/
        └── main/java/com/ecommerce/order/
            ├── client/                      # WebClient with Bearer token & Correlation ID forwarding
            ├── config/                      # SecurityConfig & OpenApiConfig
            └── security/                    # JwtService & JwtAuthenticationFilter
```

---

## 🛠️ How to Build & Run

### 1. Build via Maven Wrapper
Build and package all microservices into runnable JARs:
```bash
./mvnw clean package -DskipTests
```

### 2. Run Entire Stack with Docker Compose
Start PostgreSQL databases (`auth-db`, `product-db`, `order-db`) and microservices (`auth-service`, `product-service`, `order-service`):
```bash
docker compose up --build -d
```

Check running container status and health checks:
```bash
docker compose ps
```

### 3. Service Swagger API Documentation
Access interactive OpenAPI documentation:
- **Auth Service Swagger UI:** `http://localhost:8083/swagger-ui.html`
- **Product Service Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **Order Service Swagger UI:** `http://localhost:8082/swagger-ui.html`

---

## 🧪 Testing

Execute unit, security, and integration tests across all modules:
```bash
./mvnw clean test
```

---

## 📡 Sample REST API Requests (cURL)

### 1. User Registration (Signup)
```bash
curl -X POST http://localhost:8083/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!",
    "fullName": "Jane Doe",
    "role": "ROLE_USER"
  }'
```

### 2. User Login
```bash
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!"
  }'
```
*Returns JSON containing `accessToken` (JWT).*

---

### Authorized Service Endpoints

*Save your token: `TOKEN="eyJhbGciOiJIUzI1NiJ9..."`*

#### 3. Create Product (Product Service - Port 8081)
```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Ergonomic Mouse",
    "description": "2.4GHz Bluetooth dual-mode mouse",
    "price": 49.99,
    "stock": 100,
    "category": "Electronics"
  }'
```

#### 4. Search Products (Product Service - Port 8081)
```bash
curl -X GET "http://localhost:8081/api/v1/products?category=Electronics&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5. Place Order (Order Service - Port 8082)
*Forwards Bearer token downstream to Product Service automatically.*
```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Jane Doe",
    "customerEmail": "user@example.com",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```