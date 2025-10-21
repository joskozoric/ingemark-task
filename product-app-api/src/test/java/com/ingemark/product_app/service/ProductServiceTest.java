package com.ingemark.product_app.service;

import com.ingemark.product_app.api.model.ProductDto;
import com.ingemark.product_app.mapper.ProductMapper;
import com.ingemark.product_app.model.Product;
import com.ingemark.product_app.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private ProductDto testProductDto;
    private Product testProductEntity;

    @BeforeEach
    void setUp() {
        testProductDto = new ProductDto();
        testProductDto.setCode("ABC1234567");
        testProductDto.setName("Test Product");
        testProductDto.setPriceEur(99.99f);
        testProductDto.setIsAvailable(true);

        testProductEntity = new Product();
        testProductEntity.setId(1L);
        testProductEntity.setCode("ABC1234567");
        testProductEntity.setName("Test Product");
        testProductEntity.setPriceEur(new BigDecimal("99.99"));
        testProductEntity.setIsAvailable(true);
        testProductEntity.setCreatedAt(LocalDateTime.now());
        testProductEntity.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void productsPost_Success() {
        when(productMapper.toEntity(testProductDto)).thenReturn(testProductEntity);
        when(productRepository.save(testProductEntity)).thenReturn(testProductEntity);
        when(productMapper.toDto(testProductEntity)).thenReturn(testProductDto);

        ResponseEntity<ProductDto> response = productService.productsPost(testProductDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testProductDto.getCode(), response.getBody().getCode());

        verify(productRepository, times(1)).save(testProductEntity);
    }

    @Test
    void productsPost_UniqueCodeViolation_ThrowsBadRequest() {
        when(productMapper.toEntity(testProductDto)).thenReturn(testProductEntity);
        // mock unique constraint violation on 'code'
        when(productRepository.save(testProductEntity))
                .thenThrow(new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint \"product_code_key\""));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.productsPost(testProductDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Product with same code already exists"));

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void productsPost_OtherDataIntegrityViolation_ThrowsInternalServerError() {
        when(productMapper.toEntity(testProductDto)).thenReturn(testProductEntity);
        // mock generic db exception
        when(productRepository.save(testProductEntity))
                .thenThrow(new DataIntegrityViolationException("ERROR: cannot insert NULL into column \"name\""));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.productsPost(testProductDto));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Exception while persisting product"));

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void productsCodeGet_ProductExists_ReturnsOk() {
        String code = "ABC1234567";
        when(productRepository.findByCode(code)).thenReturn(Optional.of(testProductEntity));
        when(productMapper.toDto(testProductEntity)).thenReturn(testProductDto);

        ResponseEntity<ProductDto> response = productService.productsCodeGet(code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(code, response.getBody().getCode());
        verify(productRepository, times(1)).findByCode(code);
    }

    @Test
    void productsCodeGet_ProductDoesNotExist_ReturnsNotFound() {
        String code = "FAKECODE00";
        when(productRepository.findByCode(code)).thenReturn(Optional.empty());

        ResponseEntity<ProductDto> response = productService.productsCodeGet(code);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(productRepository, times(1)).findByCode(code);
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void productsGet_AvailableProducts_ReturnsList() {
        List<Product> entityList = Collections.singletonList(testProductEntity);
        List<ProductDto> dtoList = Collections.singletonList(testProductDto);
        boolean isAvailable = true;

        when(productRepository.findByIsAvailable(eq(isAvailable), any(Limit.class)))
                .thenReturn(entityList);
        when(productMapper.toDto(entityList)).thenReturn(dtoList);

        ResponseEntity<List<ProductDto>> response = productService.productsGet(isAvailable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());

        verify(productRepository, times(1)).findByIsAvailable(eq(isAvailable), eq(Limit.of(50)));
    }
}
