package com.ingemark.product_app.service;

import com.ingemark.product_app.api.ProductsApiDelegate;
import com.ingemark.product_app.api.model.ProductDto;
import com.ingemark.product_app.mapper.ProductMapper;
import com.ingemark.product_app.model.Product;
import com.ingemark.product_app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductsApiDelegate {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ResponseEntity<ProductDto> productsPost(ProductDto productDto) {
        try {
            Product productEntity = productRepository.save(productMapper.toEntity(productDto));
            return new ResponseEntity<>(productMapper.toDto(productEntity), HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("product_code_key")){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with same code already exists");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while persisting product", e);
        }
    }

    public ResponseEntity<ProductDto> productsCodeGet(String code) {
        Optional<Product> product = productRepository.findByCode(code);
        if(product.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(productMapper.toDto(product.get()), HttpStatus.OK);
    }

    public ResponseEntity<List<ProductDto>> productsGet(Boolean isAvailable) {
        List<Product> products = productRepository.findByIsAvailable(isAvailable, Limit.of(50));
        return new ResponseEntity<>(productMapper.toDto(products), HttpStatus.OK);
    }
}
