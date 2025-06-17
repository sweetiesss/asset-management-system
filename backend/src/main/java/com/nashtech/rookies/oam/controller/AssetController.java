package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentHistory;
import com.nashtech.rookies.oam.service.AssetService;
import com.nashtech.rookies.oam.util.ResponseUtil;
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
import java.util.UUID;

import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE;
import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE_SIZE;
import static com.nashtech.rookies.oam.constant.SortConstants.ASC;
import static com.nashtech.rookies.oam.constant.SortConstants.DEFAULT_ASSET_LIST_SORT_FIELD;
import static com.nashtech.rookies.oam.dto.api.ApiResult.success;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Assets", description = "Endpoints for retrieving and managing assets")
public class AssetController {
    private final AssetService assetService;

    @PostMapping
    @Operation(
            summary = "Create a new asset",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Asset created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<AssetResponse>> createAsset(@RequestBody @Valid AssetRequest assetRequest) {
        AssetResponse assetResponse = assetService.createAsset(assetRequest);

        ApiGenericResponse<AssetResponse> body = success(
                "Asset created successfully",
                assetResponse
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(assetResponse.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update an existing asset",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Asset updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or Validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Asset not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<AssetResponse>> updateAsset(
            @PathVariable("id") UUID id,
            @RequestBody @Valid UpdateAssetRequest assetRequest) {
        AssetResponse assetResponse = assetService.updateAsset(id, assetRequest);

        ApiGenericResponse<AssetResponse> body = ApiResult.success(
                "Asset updated successfully",
                assetResponse
        );

        return ResponseEntity.ok(body);
    }


    @GetMapping("/{id}/edit-view")
    @Operation(
            summary = "Get asset by ID for edit",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Asset retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Asset not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<AssetResponse>> getAssetByIdForEdit(
            @PathVariable("id") UUID id
    ) {
        AssetResponse assetResponse = assetService.getAssetForEdit(id);
        ApiGenericResponse<AssetResponse> body = ApiResult.success(
                "Asset retrieved successfully",
                assetResponse
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<ApiGenericResponse<APIPageableResponseDTO<AssetPageResponse>>> getAssets(
            @RequestParam(defaultValue = DEFAULT_PAGE, name = "page") Integer pageNo,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, name = "size") Integer pageSize,
            @RequestParam(defaultValue = "", name = "search", required = false) String search,
            @RequestParam(defaultValue = DEFAULT_ASSET_LIST_SORT_FIELD, name = "sort") String sortField,
            @RequestParam(defaultValue = ASC, name = "sortOrder") String sortOrder,
            @RequestParam(name = "categories", required = false) List<String> categories,
            @RequestParam(name = "states", required = false) List<String> states
    ) {
        return ResponseUtil.success(
                "Asset list retrieved successfully",
                assetService.getAssets(pageNo, pageSize, search, sortField, sortOrder, categories, states)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get asset detail by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Asset retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Asset not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<AssetResponse>> getAssetDetailById(
            @PathVariable("id") UUID id
    ) {
        AssetResponse assetResponse = assetService.getAssetDetail(id);
        ApiGenericResponse<AssetResponse> body = ApiResult.success(
                "Asset retrieved successfully",
                assetResponse
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/assignments")
    @Operation(
            summary = "Get assignment history of an asset",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Assignment history retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Asset not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<APIPageableResponseDTO<AssignmentHistory>>> getAssignmentHistory(
            @PathVariable("id") UUID id,
            @RequestParam(defaultValue = DEFAULT_PAGE, name = "page") Integer pageNo,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, name = "size") Integer pageSize
    ) {
        APIPageableResponseDTO<AssignmentHistory> assetResponse = assetService.getAssignmentHistory(id, pageNo, pageSize);
        ApiGenericResponse<APIPageableResponseDTO<AssignmentHistory>> body = ApiResult.success(
                "Assignment history retrieved successfully",
                assetResponse
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an asset by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Asset deleted successfully"),
                    @ApiResponse(responseCode = "409", description = "Asset cannot be deleted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Asset not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<ApiGenericResponse<Void>> deleteAsset(@PathVariable("id") UUID id) {
        assetService.deleteAsset(id);
        ApiGenericResponse<Void> body = ApiResult.success(
                "Asset deleted successfully",
                null
        );
        return ResponseEntity.ok(body);
    }
}
