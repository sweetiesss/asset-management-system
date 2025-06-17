package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.exception.CategoryNameAlreadyExistsException;
import com.nashtech.rookies.oam.exception.CategoryPrefixAlreadyExistsException;
import com.nashtech.rookies.oam.mapper.CategoryMapper;
import com.nashtech.rookies.oam.model.Category;
import com.nashtech.rookies.oam.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest categoryRequest;
    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Electronics");
        categoryRequest.setPrefix("EL");

        category = Category.builder()
                .id(1)
                .name("Electronics")
                .prefix("EL")
                .build();

        categoryResponse = new CategoryResponse();
        categoryResponse.setName("Electronics");
        categoryResponse.setPrefix("EL");
    }

    @Test
    void createCategory_WithValidData_ShouldCreateCategorySuccessfully() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.existsByPrefix("EL")).thenReturn(false);
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(categoryRequest);

        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).existsByPrefix("EL");
        verify(categoryRepository).save(category);
        verify(categoryMapper).toEntity(categoryRequest);
        verify(categoryMapper).toResponse(category);
        assertEquals(categoryResponse, result);
    }

    @Test
    void createCategory_WithExistingName_ShouldThrowCategoryNameAlreadyExistsException() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        CategoryNameAlreadyExistsException exception = assertThrows(CategoryNameAlreadyExistsException.class,
                () -> categoryService.createCategory(categoryRequest));

        assertEquals(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS.getMessage(), exception.getMessage());
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithExistingPrefix_ShouldThrowCategoryPrefixAlreadyExistsException() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.existsByPrefix("EL")).thenReturn(true);

        CategoryPrefixAlreadyExistsException exception = assertThrows(CategoryPrefixAlreadyExistsException.class,
                () -> categoryService.createCategory(categoryRequest));

        assertEquals(ErrorCode.CATEGORY_PREFIX_ALREADY_EXISTS.getMessage(), exception.getMessage());
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).existsByPrefix("EL");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_WithTwoExistingCategories_ShouldReturnCategoryList() {
        Category category1 = Category.builder()
                .id(1)
                .name("Electronics")
                .prefix("EL")
                .build();
        Category category2 = Category.builder()
                .id(2)
                .name("Books")
                .prefix("BK")
                .build();
        List<Category> categories = Arrays.asList(category1, category2);

        CategoryResponse response1 = new CategoryResponse();
        response1.setName("Electronics");
        response1.setPrefix("EL");
        CategoryResponse response2 = new CategoryResponse();
        response2.setName("Books");
        response2.setPrefix("BK");
        List<CategoryResponse> expectedResponses = Arrays.asList(response1, response2);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponses(categories)).thenReturn(expectedResponses);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(expectedResponses, result);
        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponses(categories);
    }

    @Test
    void getAllCategories_WithExistingCategories_ShouldReturnCategoryList() {
        List<Category> categories = Arrays.asList(category);
        List<CategoryResponse> expectedResponses = Arrays.asList(categoryResponse);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponses(categories)).thenReturn(expectedResponses);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertFalse(result.isEmpty());
        assertEquals(expectedResponses.size(), result.size());
        assertEquals(expectedResponses, result);
        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponses(categories);
    }
}
