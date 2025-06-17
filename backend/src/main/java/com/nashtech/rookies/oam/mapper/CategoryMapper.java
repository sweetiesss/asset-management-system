package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.model.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest categoryRequest);

    List<CategoryResponse> toResponses(List<Category> categories);

    CategoryResponse toResponse(Category category);
}
