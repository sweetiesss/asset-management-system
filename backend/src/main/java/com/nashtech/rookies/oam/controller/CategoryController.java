package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.request.CategoryRequest;
import com.nashtech.rookies.oam.dto.response.CategoryResponse;
import com.nashtech.rookies.oam.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Categories", description = "Endpoints for retrieving and managing categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiGenericResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        ApiGenericResponse<List<CategoryResponse>> body = ApiResult.success(
                "Retrieved category list successfully",
                categories
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(
            summary = "Create a new category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<CategoryResponse>> createCategory(@RequestBody @Valid CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);

        ApiGenericResponse<CategoryResponse> body = ApiResult.success(
                "Category created successfully",
                categoryResponse
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryResponse.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

}
