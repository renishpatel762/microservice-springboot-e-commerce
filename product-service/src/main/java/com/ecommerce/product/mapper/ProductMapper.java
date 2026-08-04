package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * ===================================================================================
 * MAPSTRUCT COMPILE-TIME MAPPING PATTERN:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    MapStruct is an annotation processor that generates plain, zero-reflection Java mapping code
 *    at compile time.
 *
 * 2. WHY IT IS BETTER THAN ALTERNATIVES:
 *    - ModelMapper / BeanUtils: Rely on reflection at runtime, making them slow and error-prone.
 *      Silent failures occur if field names differ slightly.
 *    - Manual Builder/Setter mapping: Requires writing hundreds of lines of boilerplate setter code.
 *
 * 3. SPRING INTEGRATION:
 *    `componentModel = "spring"` generates `@Component` implementation classes, enabling clean
 *    constructor injection of the mapper into service classes.
 * ===================================================================================
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    /**
     * Map request DTO to entity for new creation.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequestRecord requestRecord);

    /**
     * Map entity to response DTO record.
     */
    ProductResponseRecord toResponse(Product product);

    /**
     * Update existing entity state in-place from request DTO payload.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ProductRequestRecord requestRecord, @MappingTarget Product targetEntity);
}
