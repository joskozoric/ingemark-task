package com.ingemark.product_app.mapper;

import com.ingemark.product_app.api.model.ProductDto;
import com.ingemark.product_app.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings({
          @Mapping(source = "name", target = "name")
    })
    ProductDto toDto(Product product);
    List<ProductDto> toDto(List<Product> products);

    Product toEntity(ProductDto productDto);
    List<Product> toEntity(List<ProductDto> productDtos);
}
