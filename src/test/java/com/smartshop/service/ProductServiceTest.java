package com.smartshop.service;

import com.smartshop.dto.ProductDTO;
import com.smartshop.entity.Product;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.ProductMapper;
import com.smartshop.repository.ProductRepository;
import com.smartshop.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .description("Test Description")
                .unitPrice(new BigDecimal("99.99"))
                .stock(100)
                .category("Electronics")
                .deleted(false)
                .build();

        productDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .description("Test Description")
                .unitPrice(new BigDecimal("99.99"))
                .stock(100)
                .category("Electronics")
                .deleted(false)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsBySku(productDTO.getSku())).thenReturn(false);
        when(productMapper.toEntity(productDTO)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals(productDTO.getName(), result.getName());
        assertEquals(productDTO.getSku(), result.getSku());
        verify(productRepository).existsBySku(productDTO.getSku());
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toDTO(product);
    }

    @Test
    void createProduct_SkuAlreadyExists_ThrowsException() {
        when(productRepository.existsBySku(productDTO.getSku())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(productDTO);
        });
        verify(productRepository).existsBySku(productDTO.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
        verify(productRepository).findByIdAndDeletedFalse(999L);
    }

    @Test
    void getAllProducts_Success() {
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByDeletedFalse(pageable)).thenReturn(productPage);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        Page<ProductDTO> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(productDTO.getName(), result.getContent().get(0).getName());
        verify(productRepository).findByDeletedFalse(pageable);
    }

    @Test
    void updateProduct_Success() {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .sku("TEST-001")
                .unitPrice(new BigDecimal("149.99"))
                .stock(50)
                .category("Electronics")
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .sku("TEST-001")
                .unitPrice(new BigDecimal("149.99"))
                .stock(50)
                .category("Electronics")
                .deleted(false)
                .build();

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).updateEntityFromDTO(updateDTO, product);
        when(productRepository.save(product)).thenReturn(updatedProduct);
        when(productMapper.toDTO(updatedProduct)).thenReturn(updateDTO);

        ProductDTO result = productService.updateProduct(1L, updateDTO);

        assertNotNull(result);
        verify(productRepository).findByIdAndDeletedFalse(1L);
        verify(productMapper).updateEntityFromDTO(updateDTO, product);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(999L, productDTO);
        });
        verify(productRepository).findByIdAndDeletedFalse(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.deleteProduct(1L);

        assertTrue(product.getDeleted());
        verify(productRepository).findByIdAndDeletedFalse(1L);
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(999L);
        });
        verify(productRepository).findByIdAndDeletedFalse(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

}
