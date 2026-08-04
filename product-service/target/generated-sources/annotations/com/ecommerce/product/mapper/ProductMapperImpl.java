package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T22:23:49+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(ProductRequestRecord requestRecord) {
        if ( requestRecord == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.category( requestRecord.category() );
        product.description( requestRecord.description() );
        product.name( requestRecord.name() );
        product.price( requestRecord.price() );
        product.stock( requestRecord.stock() );

        return product.build();
    }

    @Override
    public ProductResponseRecord toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String description = null;
        BigDecimal price = null;
        Integer stock = null;
        String category = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = product.getId();
        name = product.getName();
        description = product.getDescription();
        price = product.getPrice();
        stock = product.getStock();
        category = product.getCategory();
        createdAt = product.getCreatedAt();
        updatedAt = product.getUpdatedAt();

        ProductResponseRecord productResponseRecord = new ProductResponseRecord( id, name, description, price, stock, category, createdAt, updatedAt );

        return productResponseRecord;
    }

    @Override
    public void updateEntityFromRequest(ProductRequestRecord requestRecord, Product targetEntity) {
        if ( requestRecord == null ) {
            return;
        }

        if ( requestRecord.category() != null ) {
            targetEntity.setCategory( requestRecord.category() );
        }
        if ( requestRecord.description() != null ) {
            targetEntity.setDescription( requestRecord.description() );
        }
        if ( requestRecord.name() != null ) {
            targetEntity.setName( requestRecord.name() );
        }
        if ( requestRecord.price() != null ) {
            targetEntity.setPrice( requestRecord.price() );
        }
        if ( requestRecord.stock() != null ) {
            targetEntity.setStock( requestRecord.stock() );
        }
    }
}
