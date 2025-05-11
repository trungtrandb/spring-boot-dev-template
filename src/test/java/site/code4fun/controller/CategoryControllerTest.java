package site.code4fun.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import site.code4fun.model.CategoryEntity ;
import site.code4fun.model.dto.CategoryDTO;
import site.code4fun.model.mapper.CategoryMapper;
import site.code4fun.service.CategoryService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;
    @Mock
    CategoryMapper mapper;

    @InjectMocks
    private CategoryController categoryController;


    @Test
    void testGetAllPaging() {
        // Mocking the CategoryService getPaging method to return a page of categories
        CategoryEntity  category1 = new CategoryEntity ();
        category1.setId(1L);
        category1.setName("Category A");

        CategoryEntity  category2 = new CategoryEntity();
        category2.setId(2L);
        category2.setName("Category B");
        Page<CategoryEntity> categoryPage = new PageImpl<>(List.of(category1, category2));
        Mockito.when(categoryService.getPaging(Mockito.anyMap())).thenReturn(categoryPage);

        CategoryDTO categoryDTO1 = new CategoryDTO();
        categoryDTO1.setId(1L);
        categoryDTO1.setName("Category A");

        CategoryDTO categoryDTO2 = new CategoryDTO();
        categoryDTO2.setId(1L);
        categoryDTO2.setName("Category B");
        Mockito.when(mapper.entityToDto(category1)).thenReturn(categoryDTO1);
        Mockito.when(mapper.entityToDto(category2)).thenReturn(categoryDTO2);

        // Prepare the request parameters
        Map<String, String> requestParams = Collections.emptyMap();

        // Call the controller method
        Page<CategoryDTO> result = categoryController.getAllPaging(requestParams);

        // Assert the result
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals("Category A", result.getContent().get(0).getName());
        Assertions.assertEquals("Category B", result.getContent().get(1).getName());

        // Verify that the CategoryService getPaging method was called with the appropriate request parameters
        Mockito.verify(categoryService, Mockito.times(1)).getPaging(requestParams);
    }

    @Test
    void testGetById() {
        // Mocking the CategoryService getById method to return a category
        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("Category A");

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Category A");

        Mockito.when(categoryService.getById(1L)).thenReturn(category);
        Mockito.when(mapper.entityToDto(category)).thenReturn(categoryDTO);

        // Call the controller method
        CategoryDTO result = categoryController.getById(1L);

        // Assert the result
        Assertions.assertEquals("Category A", result.getName());

        // Verify that the CategoryService getById method was called with the appropriate ID
        Mockito.verify(categoryService, Mockito.times(1)).getById(1L);
    }

    // Add more test cases for other controller methods as needed
}