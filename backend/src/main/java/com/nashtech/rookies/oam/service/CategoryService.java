package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    List<CategoryResponse> getAllCategories();
}
