package com.example.template.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.template.TestcontainersConfiguration;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Runs against real PostgreSQL with the Flyway schema applied, so it also proves the entity matches the migration.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void existsByNameAndIdNot_ignoresTheProductItself() {
        // Arrange
        Product keyboard = productRepository.save(new Product("Keyboard", null, new BigDecimal("199.00")));

        // Act
        boolean takenBySelf = productRepository.existsByNameAndIdNot("Keyboard", keyboard.getId());
        boolean takenByOther = productRepository.existsByNameAndIdNot("Keyboard", keyboard.getId() + 1);

        // Assert
        assertThat(takenBySelf).isFalse();
        assertThat(takenByOther).isTrue();
    }

    @Test
    void save_whenNameDuplicated_isRejectedByDatabase() {
        // Arrange
        productRepository.saveAndFlush(new Product("Keyboard", null, BigDecimal.ONE));

        // Act & Assert
        assertThatThrownBy(() -> productRepository.saveAndFlush(new Product("Keyboard", null, BigDecimal.TEN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
