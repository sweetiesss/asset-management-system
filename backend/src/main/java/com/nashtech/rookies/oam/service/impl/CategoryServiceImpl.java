package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.exception.CategoryNameAlreadyExistsException;
import com.nashtech.rookies.oam.exception.CategoryPrefixAlreadyExistsException;
import com.nashtech.rookies.oam.mapper.CategoryMapper;
import com.nashtech.rookies.oam.model.Category;
import com.nashtech.rookies.oam.repository.CategoryRepository;
import com.nashtech.rookies.oam.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        log.info("Creating category with name: {}", categoryRequest.getName());

        validateUniqueName(categoryRequest.getName());
        validateUniquePrefix(categoryRequest.getPrefix());

        Category category = categoryMapper.toEntity(categoryRequest);
        category = categoryRepository.save(category);

        log.info("Category created with ID: {}", category.getId());

        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponses(categoryRepository.findAll());
    }

    private void validateUniqueName(String name) {
        if(categoryRepository.existsByName(name)){
            log.warn("Category with name {} already exists", name);
            throw new CategoryNameAlreadyExistsException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS.getMessage());
        }
    }

    private void validateUniquePrefix(String prefix) {
        if(categoryRepository.existsByPrefix(prefix)){
            log.warn("Category with prefix {} already exists", prefix);
            throw new CategoryPrefixAlreadyExistsException(ErrorCode.CATEGORY_PREFIX_ALREADY_EXISTS.getMessage());
        }
    }
}
