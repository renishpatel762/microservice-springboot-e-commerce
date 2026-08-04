package com.ecommerce.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ===================================================================================
 * JPA ENTITY DESIGN PATTERN & ENTERPRISE STANDARDS:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    Represents the database table structure as an object graph within Java (Object-Relational Mapping).
 *
 * 2. EQUALS & HASHCODE BEST PRACTICE:
 *    - Never use default Lombok `@Data` or `@EqualsAndHashCode` on JPA Entities across all fields.
 *      Hibernate creates dynamic proxies for lazy loading; accessing fields directly in equals/hashCode
 *      or including lazy associations breaks collection sets (`HashSet`/`HashMap`) when entities transition
 *      between un-persisted, persisted, and detached states.
 *    - Best practice: Only include the primary key (`id`) using `onlyExplicitlyIncluded = true`.
 *
 * 3. NO ENTITY EXPOSURE OVER REST:
 *    Entities are persistent infrastructure details. Returning JPA entities in REST APIs leads to
 *    N+1 query problems, circular JSON serialization recursion, lazy initialization exceptions,
 *    and security data leaks. Always map Entities to immutable DTOs (Java Records).
 * ===================================================================================
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * JPA Lifecycle Hook executed prior to INSERT operation.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * JPA Lifecycle Hook executed prior to UPDATE operation.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
