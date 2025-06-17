package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetReturnPageRequest;
import com.nashtech.rookies.oam.dto.request.AssetReturnRequest;
import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetReturnResponse;
import com.nashtech.rookies.oam.service.AssetReturnService;
import com.nashtech.rookies.oam.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.nashtech.rookies.oam.constant.SortConstants.DEFAULT_ASSET_REPORT_LIST_SORT_FIELD;

@RestController
@RequestMapping("/api/v1/asset-returns")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Asset returns", description = "Endpoints for retrieving and managing asset return")
@Tag(name = "Asset Return", description = "Endpoints for retrieving and managing asset returns")
public class AssetReturnController {

    private final AssetReturnService assetReturnService;

    @PatchMapping("{returnId}")
    public ResponseEntity<ApiGenericResponse<AssetReturnResponse>> updateReturnRequest(
            @PathVariable UUID returnId, @RequestBody @Valid AssetReturnRequest assetReturnRequest
    ) {
        AssetReturnResponse response = assetReturnService.updateAssetReturn(returnId, assetReturnRequest);
        ApiGenericResponse<AssetReturnResponse> body = ApiResult.success("Return request updated", response);
        return ResponseEntity.ok().body(body);
    }
    @GetMapping()
    public ResponseEntity<ApiGenericResponse<APIPageableResponseDTO<AssetReturnPageResponse>>> getReturns(
            @Valid @ModelAttribute AssetReturnPageRequest request
    ) {
        if (StringUtils.isBlank(request.getSort())) {
            request.setSort(DEFAULT_ASSET_REPORT_LIST_SORT_FIELD);
        }

        return ResponseUtil.success(
                "Asset return list retrieved successfully",
                assetReturnService.getAssetReturns(request)
        );
    }
}
