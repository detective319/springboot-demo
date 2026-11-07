package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.Product;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    public ProductServiceImpl(ProductRepository repository) { this.repository = repository; }
    public List<ProductResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(ProductResponse::from).toList();
    }
    public ProductResponse findById(Long id) { return ProductResponse.from(requireProduct(id)); }
    @Transactional
    public ProductResponse create(ProductRequest request) {
        return ProductResponse.from(repository.save(new Product(request.name().trim(), request.price())));
    }
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = requireProduct(id);
        product.update(request.name().trim(), request.price());
        return ProductResponse.from(product);
    }
    @Transactional
    public void delete(Long id) { repository.delete(requireProduct(id)); }
    private Product requireProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
