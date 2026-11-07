package com.example.demo.dto;

import com.example.demo.entity.Product;
import java.io.Serializable;
import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) implements Serializable {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice());
    }
}
