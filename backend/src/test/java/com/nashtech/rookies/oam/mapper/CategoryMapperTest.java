package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.model.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {
    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toResponse_ShouldMapAllFieldsCorrectly() {
        Category category = new Category();
        category.setId(1);
        category.setName("Electronics");
        category.setPrefix("EL");

        CategoryResponse response = categoryMapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getName()).isEqualTo(category.getName());
        assertThat(response.getPrefix()).isEqualTo(category.getPrefix());
    }

    @Test
    void toResponse_ShouldHandleNullValues() {
        Category category = new Category();
        category.setId(1);

        CategoryResponse response = categoryMapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getName()).isNull();
        assertThat(response.getPrefix()).isNull();
    }

    @Test
    void toEntity_ShouldMapAllFieldsCorrectly() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setPrefix("BO");

        Category category = categoryMapper.toEntity(request);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo(request.getName());
        assertThat(category.getPrefix()).isEqualTo(request.getPrefix());
        assertThat(category.getId()).isNull();
    }

    @Test
    void toEntity_ShouldHandleNullValues() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");

        Category category = categoryMapper.toEntity(request);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo(request.getName());
        assertThat(category.getPrefix()).isNull();
        assertThat(category.getId()).isNull();
    }

    @Test
    void toResponses_ShouldMapListOfCategoriesCorrectly() {
        Category category1 = new Category();
        category1.setId(1);
        category1.setName("Electronics");
        category1.setPrefix("EL");

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Books");
        category2.setPrefix("BK");

        List<Category> categories = Arrays.asList(category1, category2);

        List<CategoryResponse> responses = categoryMapper.toResponses(categories);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);

        assertThat(responses.get(0)).isNotNull();
        assertThat(responses.get(0).getId()).isEqualTo(category1.getId());
        assertThat(responses.get(0).getName()).isEqualTo(category1.getName());
        assertThat(responses.get(0).getPrefix()).isEqualTo(category1.getPrefix());

        assertThat(responses.get(1)).isNotNull();
        assertThat(responses.get(1).getId()).isEqualTo(category2.getId());
        assertThat(responses.get(1).getName()).isEqualTo(category2.getName());
        assertThat(responses.get(1).getPrefix()).isEqualTo(category2.getPrefix());
    }

    @Test
    void toResponses_WithEmptyList_ShouldReturnEmptyList() {
        List<Category> categories = Collections.emptyList();

        List<CategoryResponse> responses = categoryMapper.toResponses(categories);

        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();
    }
}
