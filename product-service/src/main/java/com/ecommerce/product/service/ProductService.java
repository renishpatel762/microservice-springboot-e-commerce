package com.ecommerce.product.service;

import com.ecommerce.product.dto.PagedResponseRecord;
import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.dto.ProductSearchCriteria;
import com.ecommerce.product.dto.StockReductionRequestRecord;
import com.ecommerce.product.dto.StockReductionResponseRecord;

/**
 * Service contract for Product management domain operations.
 */
public interface ProductService {

    ProductResponseRecord createProduct(ProductRequestRecord requestRecord);

    ProductResponseRecord updateProduct(Long id, ProductRequestRecord requestRecord);

    void deleteProduct(Long id);

    ProductResponseRecord getProductById(Long id);

    PagedResponseRecord<ProductResponseRecord> searchProducts(ProductSearchCriteria criteria);

    StockReductionResponseRecord reduceStock(StockReductionRequestRecord requestRecord);
}
