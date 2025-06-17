package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetReturnPageRequest;
import com.nashtech.rookies.oam.dto.request.AssetReturnRequest;
import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetReturnResponse;

import java.util.UUID;

public interface AssetReturnService {
    AssetReturnResponse createAssetReturn(UUID assignmentId);

    APIPageableResponseDTO<AssetReturnPageResponse> getAssetReturns(AssetReturnPageRequest request);

    AssetReturnResponse updateAssetReturn(UUID returnId, AssetReturnRequest assetReturnRequest);
}
