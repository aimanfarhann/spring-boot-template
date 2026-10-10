package com.example.template.product;

import com.example.template.common.exception.ResourceConflictException;
import com.example.template.common.exception.ResourceNotFoundException;
import com.example.template.product.dto.ProductRequest;
import com.example.template.product.dto.ProductResponse;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product use cases. Returns DTOs rather than entities so nothing lazy escapes the transaction
 * (open-in-view is disabled).
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final String RESOURCE_NAME = "Product";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll(Sort.by("id")).stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.from(getExistingProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw duplicateNameConflict(request.name());
        }
        Product product = new Product(request.name(), request.description(), request.price());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getExistingProduct(id);
        if (productRepository.existsByNameAndIdNot(request.name(), id)) {
            throw duplicateNameConflict(request.name());
        }
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(getExistingProduct(id));
    }

    private Product getExistingProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
    }

    private static ResourceConflictException duplicateNameConflict(String name) {
        return new ResourceConflictException("A product named '%s' already exists".formatted(name));
    }
}
