package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentHistory;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.enums.AssetState;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    AssetResponse createAsset(AssetRequest assetRequest);

    AssetResponse updateAsset(UUID id, UpdateAssetRequest updateAssetRequest);

    AssetResponse getAssetForEdit(UUID id);

    AssetResponse getAssetDetail(UUID id);

    APIPageableResponseDTO<AssignmentHistory> getAssignmentHistory(UUID id, Integer pageNo, Integer pageSize);
    APIPageableResponseDTO<AssetPageResponse> getAssets(
            int pageNo,
            int pageSize,
            String search,
            String sortField,
            String sortOrder,
            List<String> categories,
            List<String> states
    );

    Asset getAssetByIdForUpdate(UUID id);

    void deleteAsset(UUID id);

    boolean isAssetAvailable(Asset asset);

    void updateAssetState(Asset asset, AssetState state);
}
