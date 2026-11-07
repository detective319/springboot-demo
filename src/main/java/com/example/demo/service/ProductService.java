package com.example.demo.service;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

public interface ProductService {
    List<ProductResponse> findAll();
    ProductResponse findById(@Positive Long id);
    ProductResponse create(@Valid ProductRequest request);
    ProductResponse update(@Positive Long id, @Valid ProductRequest request);
    void delete(@Positive Long id);
}
