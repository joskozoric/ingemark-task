package com.ingemark.product_app.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingemark.product_app.api.model.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableWireMock({
        @ConfigureWireMock(
                name = "hnb",
                port = 5555,
                filesUnderClasspath = "wiremock"
        )
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto productDto;
    private final String NEW_CODE = "NEW1234567";
    private final String EXISTING_CODE = "PRODUCT001"; // from the V2__insert_sample_products.sql
    private final String INVALID_CODE = "INVALID";

    private final String AUTH_USER = "user";
    private final String AUTH_PASS = "password";

    @BeforeEach
    void setUp() {
        productDto = new ProductDto();
        productDto.setCode(NEW_CODE);
        productDto.setName("Test Product");
        productDto.setPriceEur(10.50f);
        productDto.setIsAvailable(true);
    }

    @Test
    @Transactional
    void productsPost_Success_Returns201Created() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .with(httpBasic(AUTH_USER, AUTH_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(NEW_CODE))
                .andExpect(jsonPath("$.price_eur").value(10.50));
    }

    @Test
    void productsPost_InvalidInput_Returns400BadRequest() throws Exception {
        ProductDto invalidProduct = new ProductDto();
        invalidProduct.setCode(INVALID_CODE);
        invalidProduct.setPriceEur(90f);
        invalidProduct.setName("Invalid");
        invalidProduct.setIsAvailable(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .with(httpBasic(AUTH_USER, AUTH_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("must match \"^[a-zA-Z0-9]{10}$\""));
    }

    @Test
    void productsCodeGet_ProductExists_Returns200Ok() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/{code}", EXISTING_CODE)
                        .with(httpBasic(AUTH_USER, AUTH_PASS))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(EXISTING_CODE));
    }

    @Test
    void productsCodeGet_ProductNotFound_Returns404NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/products/{code}", "NONEXIST12")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(AUTH_USER, AUTH_PASS)))
                .andExpect(status().isNotFound());
    }

    @Test
    void productsCodeGet_InvalidCodeFormat_Returns400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/{code}", INVALID_CODE)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(AUTH_USER, AUTH_PASS)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("must match \"^[a-zA-Z0-9]{10}$\""));
    }

    @Test
    void productsGet_AvailableFilter_Returns200OkAndFilteredList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .param("is_available", String.valueOf(false))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(AUTH_USER, AUTH_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // fixed count from the V2__insert_sample_products.sql
    }

    @Test
    void productsGet_NoFilter_Returns200OkAndFullList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(AUTH_USER, AUTH_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5)); // fixed count from the V2__insert_sample_products.sql
    }
}
