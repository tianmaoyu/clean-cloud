package org.clean.inventory.service;

import org.clean.inventory.entity.Category;
import org.clean.inventory.entity.Product;
import org.clean.inventory.mapper.CategoryMapper;
import org.clean.inventory.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataGeneratorServiceTest {

    @Autowired
    private DataGeneratorService service;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;
    @Test
    void category() {
        Category category = categoryMapper.selectById(1);
        assertNull(category);
    }

    @Test
    void product() {
        Product product = productMapper.selectById(1);
        assertNull(product);
    }

    @Test
    void generateCategories() {
//        service.generateCategories(1000);
    }


    @Test
    void generateProducts() {
//        service.generateProducts(100_000_000);
    }

    @Test
    void generateProducts_threadPool() {
//        service.generateProducts_threadPool(100);
    }
}