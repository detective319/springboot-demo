package com.example.demo;

import com.example.demo.dto.*;
import com.example.demo.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {
    @Autowired TestRestTemplate rest;
    @Autowired ProductRepository repository;

    @BeforeEach
    void resetDatabase() { repository.deleteAll(); }

    @Test
    void productCrudPersistsChangesAndReturnsNotFoundAfterDelete() {
        var created = rest.postForEntity("/api/products",
                new ProductRequest("Coffee", new BigDecimal("80.00")), ProductResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String location = created.getHeaders().getLocation().toString();
        assertThat(rest.getForObject(location, ProductResponse.class).name()).isEqualTo("Coffee");
        rest.put(location, new ProductRequest("Tea", new BigDecimal("50.00")));
        assertThat(rest.getForObject(location, ProductResponse.class).name()).isEqualTo("Tea");
        assertThat(rest.getForObject("/api/products", ProductResponse[].class)).hasSize(1);
        var deleted = rest.exchange(location, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(rest.getForEntity(location, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void invalidProductDoesNotReachDatabase() {
        var response = rest.postForEntity("/api/products",
                new ProductRequest(" ", new BigDecimal("-1.00")), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(repository.count()).isZero();
    }

    @Test
    void wicketRendersProductsCreatedThroughRestAndLeavesApiAccessible() {
        rest.postForEntity("/api/products",
                new ProductRequest("Wicket integration", new BigDecimal("12.50")), ProductResponse.class);
        var page = rest.getForEntity("/ui/", String.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("Wicket integration", "<td>12.50</td>");
        assertThat(rest.getForEntity("/api/products", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
