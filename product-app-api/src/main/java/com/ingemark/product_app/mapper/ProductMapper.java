package com.ingemark.product_app.mapper;

import com.ingemark.product_app.api.model.ProductDto;
import com.ingemark.product_app.external.hnb.HnbCommunicatorService;
import com.ingemark.product_app.model.Product;
import com.ingemark.product_app.util.CurrencyConversionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        imports = {CurrencyConversionUtil.class}
)
public abstract class ProductMapper {

    protected HnbCommunicatorService hnbCommunicatorService;

    @Autowired
    protected void setHnbCommunicatorService(HnbCommunicatorService hnbCommunicatorService) {
        this.hnbCommunicatorService = hnbCommunicatorService;
    }

    @Mapping(target = "priceUsd", expression = "java(CurrencyConversionUtil.convert(product.getPriceEur(), hnbCommunicatorService.getMiddleRateUSD()))")
    public abstract ProductDto toDto(Product product);
    public abstract List<ProductDto> toDto(List<Product> products);

    public abstract Product toEntity(ProductDto productDto);
}
