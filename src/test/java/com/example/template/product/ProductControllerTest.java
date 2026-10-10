package com.example.template.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.template.common.exception.ResourceNotFoundException;
import com.example.template.product.dto.ProductRequest;
import com.example.template.product.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Web-layer slice: exercises routing, JSON binding, validation and error mapping with the service mocked out.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final ProductResponse KEYBOARD =
            new ProductResponse(1L, "Keyboard", "Mechanical keyboard", new BigDecimal("199.00"));

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void findById_whenProductExists_returnsProduct() {
        // Arrange
        when(productService.findById(1L)).thenReturn(KEYBOARD);

        // Act & Assert
        assertThat(mockMvc.get().uri("/api/v1/products/1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.name")
                .isEqualTo("Keyboard");
    }

    @Test
    void findById_whenProductMissing_returnsProblemDetail404() {
        // Arrange
        when(productService.findById(42L)).thenThrow(new ResourceNotFoundException("Product", 42L));

        // Act & Assert
        assertThat(mockMvc.get().uri("/api/v1/products/42"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("Product with id 42 was not found");
    }

    @Test
    void create_whenRequestValid_returns201WithLocation() {
        // Arrange
        when(productService.create(any(ProductRequest.class))).thenReturn(KEYBOARD);

        // Act & Assert
        assertThat(mockMvc.post()
                        .uri("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Keyboard", "description": "Mechanical keyboard", "price": 199.00}
                                """))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/api/v1/products/1");
    }

    @Test
    void create_whenRequestInvalid_returns400WithFieldErrors() {
        // Act & Assert
        assertThat(mockMvc.post()
                        .uri("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "price": -1}
                                """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors")
                .asMap()
                .containsOnlyKeys("name", "price");
        verifyNoInteractions(productService);
    }
}
