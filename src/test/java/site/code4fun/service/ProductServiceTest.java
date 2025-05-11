package site.code4fun.service;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import site.code4fun.model.Product;
import site.code4fun.repository.jpa.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
  
  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private ProductService productService;
  private final BigDecimal price = BigDecimal.valueOf(10000);
  
  @Test
  void testUpdateProduct() {

    Product productRequest = new Product();
    productRequest.setId(1L);
    productRequest.setName("Product A");
    productRequest.setPrice(price);


    Mockito.when(productRepository.save(productRequest)).thenReturn(productRequest);
    Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(productRequest));
    
    // Call the service method
    Product result = productService.create(productRequest);
    
    // Assert the result
    Assertions.assertEquals("Product A", result.getName());
    Assertions.assertEquals(price, result.getPrice());
    
    // Verify that the ProductRepository save method was called
    Mockito.verify(productRepository, Mockito.times(1)).save(productRequest);
  }
  
  @Test
  void testGetPaging() {
    // Mocking the ProductRepository findAll method with paging and returning a page of products

    Product productA = new Product();
    productA.setId(1L);
    productA.setName("Product A");
    productA.setPrice(price);
    List<Product> products = new ArrayList<>();
    products.add(productA);
    Page<Product> productPage = new PageImpl<>(products);
    Mockito.when(productRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(productPage);
    
    // Prepare the request parameters
    Map<String, String> requestParams = Map.of("page", "0", "size", "10");
    
    // Call the service method
    Page<Product> result = productService.getPaging(requestParams);
    
    // Assert the result
    Assertions.assertEquals(1, result.getTotalElements());
    Assertions.assertEquals(0, result.getNumber());
    Assertions.assertEquals(1, result.getContent().size());
    
    // Verify that the ProductRepository findAll method was called with the appropriate PageRequest
    Mockito.verify(productRepository, Mockito.times(1)).findAll(Mockito.any(PageRequest.class));
  }
  
  @Test
  void testFindAllById() {
    // Mocking the ProductRepository findAllById method to return a list of products
    List<Product> products = new ArrayList<>();

    Product productA = new Product();
    productA.setId(1L);
    productA.setName("Product A");
    productA.setPrice(price);
    products.add(productA);

    Product productB = new Product();
    productB.setId(2L);
    productB.setName("Product B");
    productB.setPrice(BigDecimal.valueOf(2).multiply(price));
    products.add(productB);

    Collection<Long> ids = Arrays.asList(1L, 2L);
    Mockito.when(productRepository.findAllById(ids)).thenReturn(products);
    
    List<Product> result = productService.findAllById(ids);
    
    Assertions.assertEquals(2, result.size());
    Mockito.verify(productRepository, Mockito.times(1)).findAllById(ids);
  }
}