# Production-Ready Enterprise Spring Boot Microservices Architecture

A real-world, enterprise-grade E-Commerce backend architecture built with **Java 21** and **Spring Boot 3.3.x**, featuring clean package organization, immutable DTO records, MapStruct compile-time mappers, Flyway database migrations, WebClient inter-service communication with correlation ID propagation, MDC structured logging, and Docker containerization.

---

## 🏛️ System Architecture

```
                                +---------------------------+
                                |  API Client / Swagger UI  |
                                +-------------+-------------+
                                              |
                     +------------------------+------------------------+
                     | (HTTP GET/POST/PUT/DELETE)                      | (HTTP GET/POST/PUT)
                     v                                                 v
         +-----------------------+                         +-----------------------+
         |    Product Service    | <--- WebClient REST --- |     Order Service     |
         |      Port 8081        |   (Stock Check/Reduce)  |       Port 8082       |
         +-----------+-----------+                         +-----------+-----------+
                     |                                                 |
                     v                                                 v
         +-----------------------+                         +-----------------------+
         |  product-db (Postgres)|                         |   order-db (Postgres) |
         |       Port 5432       |                         |       Port 5433       |
         +-----------------------+                         +-----------------------+
```

---

## 🚀 Tech Stack & Design Decisions

| Category | Technology | Rationale & Architectural Choice |
|---|---|---|
| **Runtime** | Java 21 (LTS) | Modern Java features: Records, Sealed types, Pattern Matching, Virtual Threads readiness. |
| **Framework** | Spring Boot 3.3.5 | Production baseline implementing Jakarta EE 10 specifications. |
| **Persistence** | Spring Data JPA / Hibernate | Repository pattern abstraction with JPA dynamic specifications. Strictly isolated DB schemas per service. |
| **Database Migrations**| Flyway | Version-controlled SQL scripts (`db/migration/V1__...sql`). Production `ddl-auto=validate`. |
| **Mapping Layer** | MapStruct 1.6.3 | Zero-reflection, compile-time type-safe object mapping between Entities and Records. |
| **REST Client** | Spring WebClient | Non-blocking reactive REST client with configured connection/read timeouts and MDC Correlation ID header forwarding. |
| **API Documentation** | Springdoc OpenAPI 3.0 | Interactive OpenAPI & Swagger UI (`/swagger-ui.html`). |
| **Observability** | SLF4J + MDC + Servlet Filters | Automatic UUID Correlation ID generation (`X-Correlation-ID`) injected into MDC logs and forwarded downstream. |
| **Containerization** | Docker & Docker Compose | Multi-stage Dockerfiles utilizing Eclipse Temurin JRE base images with non-root security execution. |

---

## 📚 Key Architectural Patterns & Educational Guide

### 1. Immutable DTOs via Java Records (`record`)
- **Why it exists:** Introduced in Java 14/16 as shallowly immutable data carriers.
- **Why it is better than alternatives:** Traditional JavaBeans allow accidental mutation during service processing. Records enforce thread-safe immutability at the syntax level.
- **Common Mistake:** Using raw JPA Entities as REST Request/Response bodies. This causes N+1 queries, lazy initialization exceptions, circular serialization loops, and data security leaks.

### 2. Constructor Injection vs Field Injection (`@Autowired`)
- **Why it exists:** Dependency Inversion Principle (SOLID).
- **Why it is better than alternatives:** Field injection (`@Autowired private ProductRepository repo;`) conceals circular dependencies, prevents field immutability (`final`), and breaks unit testing without Spring context reflection.
- **Enterprise Rule:** All components use Lombok `@RequiredArgsConstructor` for explicit final field constructor injection.

### 3. MapStruct Compile-Time Mappers vs Reflection
- **Why it exists:** Code generation engine producing plain Java mapping methods at compile time.
- **Why it is better than alternatives:** Tools like `ModelMapper` or `BeanUtils.copyProperties` rely on runtime reflection, introducing CPU overhead and silent field mismatch bugs.
- **When to use:** Converting Entity <-> Request/Response DTOs.

### 4. Dynamic JPA Specifications (`Specification<T>`)
- **Why it exists:** Programmatic, type-safe API for constructing SQL `WHERE` predicates dynamically.
- **Why it is better than alternatives:** Eliminates writing dozens of optional `@Query` repository methods for every combination of search parameters (category, name substring, price ranges).

### 5. Flyway Database Migrations
- **Why it exists:** Version-controlled database schema management across environments (local, staging, prod).
- **Why it is better than alternatives:** `hibernate.ddl-auto=update` in production can drop columns or corrupt production tables. Flyway guarantees repeatable, audited schema migrations (`V1__...sql`).

### 6. Centralized Error Handling (`@RestControllerAdvice`)
- **Why it exists:** Intercepts exceptions globally using Spring AOP, mapping them to uniform HTTP response status codes (400, 404, 503) and standard JSON error envelopes.
- **Common Mistake:** Catching generic `Exception` without logging stack traces or returning HTTP 200 OK with an error body ("200 Anti-pattern").

### 7. Spring WebClient for Inter-Service REST
- **Why it exists:** Modern functional HTTP client replacing legacy `RestTemplate`.
- **Key Features:** Fine-grained connection timeouts, reactive body extractors, status code mapping (`.onStatus`), and custom `ExchangeFilterFunction` for propagating correlation headers downstream.

### 8. MDC (Mapped Diagnostic Context) & Distributed Tracing
- **Why it exists:** Captures incoming `X-Correlation-ID` (or generates a UUID) and attaches it to SLF4J `ThreadLocal` MDC context.
- **Enterprise Rule:** Always clear MDC in a `finally` block (`MDC.remove("correlationId")`) to prevent ThreadLocal data leakage when servlet container thread pools reuse threads!

---

## 📁 Repository Structure

```
microservice-springboot-e-commerce/
├── pom.xml                                  # Parent POM managing dependencies
├── mvnw & mvnw.cmd                          # Self-contained Maven Wrappers
├── docker-compose.yml                       # Docker Compose infrastructure setup
├── README.md                                # System Documentation
├── product-service/
│   ├── Dockerfile                           # Multi-stage Docker build
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/ecommerce/product/
│       │   ├── ProductServiceApplication.java
│       │   ├── config/                      # OpenAPI specs
│       │   ├── controller/                  # Thin REST endpoints
│       │   ├── dto/                         # Java Records (Request, Response, Criteria)
│       │   ├── entity/                      # Product JPA Entity
│       │   ├── exception/                   # Product exceptions & GlobalExceptionHandler
│       │   ├── filter/                      # Correlation ID & Request Logging filters
│       │   ├── mapper/                      # MapStruct mappers
│       │   ├── repository/                  # JPA repository & Specification builders
│       │   └── service/                     # ProductService logic & interface
│       └── main/resources/
│           ├── application.yml              # Profile configs (local, dev, prod)
│           └── db/migration/                # Flyway V1__create_products_table.sql
└── order-service/
    ├── Dockerfile                           # Multi-stage Docker build
    ├── pom.xml
    └── src/
        ├── main/java/com/ecommerce/order/
        │   ├── OrderServiceApplication.java
        │   ├── client/                      # WebClient ProductServiceClient & config
        │   ├── config/                      # OpenAPI specs
        │   ├── controller/                  # REST endpoints
        │   ├── dto/                         # Order Request/Response Records
        │   ├── entity/                      # Order & OrderItem JPA Entities
        │   ├── enums/                       # OrderStatus enum
        │   ├── exception/                   # Domain exceptions & GlobalExceptionHandler
        │   ├── filter/                      # Correlation ID & Request Logging filters
        │   ├── mapper/                      # MapStruct mappers
        │   ├── repository/                  # JPA repository & Specification builders
        │   └── service/                     # OrderService logic & interface
        └── main/resources/
            ├── application.yml              # Profile configs
            └── db/migration/                # Flyway V1__create_orders_table.sql
```

---

## 🛠️ How to Build & Run

### 1. Build via Maven Wrapper
Build and package both microservices into runnable JARs:
```bash
./mvnw clean package -DskipTests
```

### 2. Run Entire Stack with Docker Compose
Start PostgreSQL databases (`product-db`, `order-db`) and microservices (`product-service`, `order-service`):
```bash
docker compose up --build -d
```

Check running container status and health checks:
```bash
docker compose ps
```

### 3. Service Swagger API Documentation
Once running, access interactive OpenAPI documentation:
- **Product Service Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **Order Service Swagger UI:** `http://localhost:8082/swagger-ui.html`

---

## 🧪 Testing

Execute unit and controller slice tests across all modules:
```bash
./mvnw test
```

---

## 📡 Sample REST API Requests (cURL)

### Product Service (Port 8081)

#### 1. Create a New Product
```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Ergonomic Mouse",
    "description": "2.4GHz Bluetooth dual-mode mouse",
    "price": 49.99,
    "stock": 100,
    "category": "Electronics"
  }'
```

#### 2. Search Products with Filtering & Pagination
```bash
curl -X GET "http://localhost:8081/api/v1/products?category=Electronics&name=mouse&minPrice=10&maxPrice=100&page=0&size=10&sortBy=price&sortDir=ASC"
```

#### 3. Get Product by ID
```bash
curl -X GET http://localhost:8081/api/v1/products/1
```

---

### Order Service (Port 8082)

#### 1. Place a New Order
*Validates product availability with Product Service, reduces stock, and snapshots item details.*
```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice Smith",
    "customerEmail": "alice.smith@example.com",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

#### 2. Get Order Details
```bash
curl -X GET http://localhost:8082/api/v1/orders/1
```

#### 3. Cancel an Order
```bash
curl -X PUT http://localhost:8082/api/v1/orders/1/cancel
```

---

## 🔮 Future Production Recommendations

1. **Distributed Tracing:** Integrate OpenTelemetry / Spring Cloud Sleuth with Zipkin/Jaeger for tracing across microservices.
2. **Resilience & Circuit Breakers:** Add Resilience4j circuit breakers and rate limiters to `ProductServiceClient` calls.
3. **Event-Driven Saga Pattern:** Migrate inventory deduction to an asynchronous Kafka/RabbitMQ event pipeline with compensating transactions for high-throughput order processing.