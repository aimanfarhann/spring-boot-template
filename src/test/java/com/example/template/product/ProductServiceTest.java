package com.example.template.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.template.common.exception.ResourceConflictException;
import com.example.template.common.exception.ResourceNotFoundException;
import com.example.template.product.dto.ProductRequest;
import com.example.template.product.dto.ProductResponse;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final ProductRequest KEYBOARD_REQUEST =
            new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("199.00"));

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findById_whenProductMissing_throwsNotFound() {
        // Arrange
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product with id 42 was not found");
    }

    @Test
    void create_whenNameIsUnique_savesAndReturnsProduct() {
        // Arrange
        when(productRepository.existsByName("Keyboard")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.create(KEYBOARD_REQUEST);

        // Assert
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.price()).isEqualByComparingTo("199.00");
    }

    @Test
    void create_whenNameAlreadyExists_throwsConflictWithoutSaving() {
        // Arrange
        when(productRepository.existsByName("Keyboard")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.create(KEYBOARD_REQUEST)).isInstanceOf(ResourceConflictException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_whenProductExists_appliesAllFields() {
        // Arrange
        Product existingProduct = new Product("Old name", null, BigDecimal.ONE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByNameAndIdNot("Keyboard", 1L)).thenReturn(false);

        // Act
        productService.update(1L, KEYBOARD_REQUEST);

        // Assert
        assertThat(existingProduct.getName()).isEqualTo("Keyboard");
        assertThat(existingProduct.getDescription()).isEqualTo("Mechanical keyboard");
        assertThat(existingProduct.getPrice()).isEqualByComparingTo("199.00");
    }

    @Test
    void update_whenNameTakenByAnotherProduct_throwsConflict() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product("Old name", null, BigDecimal.ONE)));
        when(productRepository.existsByNameAndIdNot("Keyboard", 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.update(1L, KEYBOARD_REQUEST))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void delete_whenProductMissing_throwsNotFound() {
        // Arrange
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.delete(42L)).isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).delete(any());
    }
}
