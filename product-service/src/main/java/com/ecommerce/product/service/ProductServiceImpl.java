package com.ecommerce.product.service;

import com.ecommerce.product.dto.PagedResponseRecord;
import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.dto.ProductSearchCriteria;
import com.ecommerce.product.dto.StockReductionRequestRecord;
import com.ecommerce.product.dto.StockReductionResponseRecord;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ===================================================================================
 * SERVICE LAYER DESIGN PATTERNS & TRANSACTION MANAGEMENT:
 * ===================================================================================
 * 1. CONSTRUCTOR INJECTION (SOLID Dependency Inversion):
 *    - Lombok `@RequiredArgsConstructor` generates a constructor for all `final` fields.
 *    - Field Injection (`@Autowired private ProductRepository repo;`) is prohibited in enterprise code because:
 *      a) Makes unit testing impossible without Spring container reflection.
 *      b) Allows mutability (fields cannot be declared `final`).
 *      c) Conceals circular dependency smell.
 *
 * 2. TRANSACTIONAL BOUNDARIES (@Transactional):
 *    - Declarative transaction boundaries manage DB transactions via Spring AOP proxy.
 *    - `@Transactional(readOnly = true)` optimizes database memory buffers, disables Hibernate dirty
 *      checking, and routes read queries to read-replicas in clustered DB environments.
 * ===================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseRecord createProduct(final ProductRequestRecord requestRecord) {
        log.info("Creating new product with name: '{}' in category: '{}'", requestRecord.name(), requestRecord.category());

        Product entity = productMapper.toEntity(requestRecord);
        Product savedEntity = productRepository.save(entity);

        log.info("Successfully created product with assigned ID: {}", savedEntity.getId());
        return productMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional
    public ProductResponseRecord updateProduct(final Long id, final ProductRequestRecord requestRecord) {
        log.info("Updating product ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productMapper.updateEntityFromRequest(requestRecord, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Successfully updated product ID: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(final Long id) {
        log.info("Deleting product ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
        log.info("Successfully deleted product ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseRecord getProductById(final Long id) {
        log.debug("Fetching product by ID: {}", id);

        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseRecord<ProductResponseRecord> searchProducts(final ProductSearchCriteria criteria) {
        log.debug("Searching products with criteria: {}", criteria);

        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDir());
        Sort sort = Sort.by(direction, criteria.sortBy());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        Specification<Product> spec = ProductSpecification.buildSpecification(criteria);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponseRecord> content = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return new PagedResponseRecord<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    @Transactional
    public StockReductionResponseRecord reduceStock(final StockReductionRequestRecord requestRecord) {
        log.info("Processing stock reduction request for product ID: {}, quantity: {}", requestRecord.productId(), requestRecord.quantity());

        Product product = productRepository.findById(requestRecord.productId())
                .orElseThrow(() -> new ProductNotFoundException(requestRecord.productId()));

        if (product.getStock() < requestRecord.quantity()) {
            throw new InsufficientStockException(product.getId(), requestRecord.quantity(), product.getStock());
        }

        int updatedStock = product.getStock() - requestRecord.quantity();
        product.setStock(updatedStock);
        productRepository.save(product);

        log.info("Stock reduced for product ID: {}. Remaining stock: {}", product.getId(), updatedStock);

        return new StockReductionResponseRecord(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}
