package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentHistory;
import com.nashtech.rookies.oam.exception.*;
import com.nashtech.rookies.oam.mapper.AssetMapper;
import com.nashtech.rookies.oam.mapper.AssignmentMapper;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Category;
import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.projection.AssignmentWithReturnDate;
import com.nashtech.rookies.oam.projection.EditAssetProjection;
import com.nashtech.rookies.oam.repository.AssetRepository;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.CategoryRepository;
import com.nashtech.rookies.oam.service.AssetCodeGeneratorService;
import com.nashtech.rookies.oam.service.AssetService;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.specification.AssetSpecification;
import com.nashtech.rookies.oam.util.PageUtil;
import com.nashtech.rookies.oam.util.SortUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AssetServiceImpl implements AssetService {
    AuthService authService;
    AssetRepository assetRepository;
    AssetMapper assetMapper;
    AssignmentMapper assignmentMapper;
    CategoryRepository categoryRepository;
    AssignmentRepository assignmentRepository;
    AssetCodeGeneratorService assetCodeGeneratorService;

    @Transactional
    @Override
    public AssetResponse createAsset(AssetRequest assetRequest) {
        User userAuth = authService.getAuthenticatedUser();
        log.info("User {} is creating asset {}", userAuth.getUsername(), assetRequest);

        Location location = userAuth.getLocation();
        Category category = findCategory(assetRequest.getCategoryId());
        String assetCode = assetCodeGeneratorService.generateAssetCode(category.getPrefix());

        Asset asset = assetMapper.toEntity(assetRequest);
        asset.setLocation(location);
        asset.setCategory(category);
        asset.setCode(assetCode);
        asset = assetRepository.save(asset);

        log.info("Asset {} created successfully", asset.getId());
        return assetMapper.toResponse(asset);
    }

    @Transactional
    @Override
    public AssetResponse updateAsset(UUID id, UpdateAssetRequest updateAssetRequest) {
        User userAuth = authService.getAuthenticatedUser();
        Asset asset = getAssetByIdForUpdate(id);

        validateUpdatable(userAuth, asset);
        Asset updatedAsset = buildUpdatedAssignment(asset, updateAssetRequest);

        try {
            assetRepository.save(updatedAsset);
            logAssetUpdate(updatedAsset, userAuth);
            return assetMapper.toResponse(asset);
        } catch (OptimisticLockingFailureException e) {
            log.error("Asset with id {} is being modified by another transaction", id);
            throw new AssetBeingModifiedException(ErrorCode.ASSET_BEING_MODIFIED.getMessage());
        }
    }

    private void logAssetUpdate(Asset updatedAsset, User userAuth) {
        log.info("User {} is updating assignment {}", userAuth.getUsername(), updatedAsset.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public AssetResponse getAssetForEdit(UUID id) {
        EditAssetProjection asset = assetRepository.findProjectedById(id)
                .orElseThrow(() -> new AssetNotFoundException(ErrorCode.ASSET_NOT_FOUND.getMessage()));

        return assetMapper.toResponseEdit(asset);
    }

    @Transactional(readOnly = true)
    @Override
    public AssetResponse getAssetDetail(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(ErrorCode.ASSET_NOT_FOUND.getMessage()));

        return assetMapper.toResponse(asset);
    }

    @Transactional(readOnly = true)
    @Override
    public APIPageableResponseDTO<AssignmentHistory> getAssignmentHistory(UUID assetId, Integer pageNo, Integer pageSize) {
        PageRequest pageRequest = PageUtil.buildPageRequest(pageNo, pageSize);
        List<String> defaultStatuses = List.of(
                AssignmentStatusType.ACCEPTED.getDbName(),
                AssignmentStatusType.COMPLETED.getDbName()
        );
        Page<AssignmentWithReturnDate> assignmentPage = assignmentRepository.findAllWithReturnDateByAsset_Id(assetId, defaultStatuses, pageRequest);
        Page<AssignmentHistory> historyPage = assignmentPage.map(assignmentMapper::toAssignmentHistory);

        return new APIPageableResponseDTO<>(historyPage);
    }

    @Override
    public APIPageableResponseDTO<AssetPageResponse> getAssets(
            int pageNo,
            int pageSize,
            String search,
            String sortField,
            String sortOrder,
            List<String> categories,
            List<String> states
    ) {
        if (CollectionUtils.isEmpty(states)) {
            states = getDefaultFilterStates();
        }

        User currentUser = authService.getAuthenticatedUser();

        Sort sort = SortUtil.buildAssetSort(sortField, sortOrder);

        Pageable pageable = PageRequest.of(Math.max(pageNo, 0), Math.max(pageSize, 1), sort);

        Page<Asset> assetPage = assetRepository.findAll(
                AssetSpecification.build(search, categories, states, currentUser.getLocation()),
                pageable
        );

        Page<AssetPageResponse> dtoPage = assetPage.map(assetMapper::toAssetPageResponseDto);
        return new APIPageableResponseDTO<>(dtoPage);
    }

    @Override
    public Asset getAssetByIdForUpdate(UUID id) {
            return assetRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new AssetNotFoundException(ErrorCode.ASSET_NOT_FOUND.getMessage()));
    }

    @Transactional
    @Override
    public void deleteAsset(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(ErrorCode.ASSET_NOT_FOUND.getMessage()));

        User currentUser = authService.getAuthenticatedUser();
        validateDeletable(currentUser, asset);

        try {
            assetRepository.delete(asset);
            log.info("User {} is deleting asset {}", currentUser.getUsername(), id);
        } catch (OptimisticLockingFailureException e) {
            throw new AssetNotFoundException(ErrorCode.ASSET_NOT_FOUND.getMessage());
        }

    }

    @Override
    public boolean isAssetAvailable(Asset asset) {
        return asset.getState() == AssetState.AVAILABLE;
    }

    @Override
    @Transactional
    public void updateAssetState(Asset asset, AssetState state) {
        asset.setState(state);
        assetRepository.save(asset);
    }

    private List<String> getDefaultFilterStates() {
        return List.of(
                AssetState.AVAILABLE.name(),
                AssetState.NOT_AVAILABLE.name(),
                AssetState.ASSIGNED.name()
        );
    }

    private void validateDeletable(User user, Asset asset) {
        boolean sameLocation = user.getLocation().equals(asset.getLocation());
        if (!sameLocation) {
            log.warn("User {} is not allowed to delete asset {} due to location mismatch", user.getUsername(), asset.getId());
            throw new AssetNotDeletableException(ErrorCode.USER_AND_ASSET_LOCATION_MISMATCH.getMessage());
        }

        boolean hasAssignments = assignmentRepository.existsByAsset(asset);

        if (hasAssignments) {
            log.warn("Asset with id {} cannot be deleted because it has assignments", asset.getId());
            throw new AssetNotDeletableException(ErrorCode.ASSET_NOT_DELETABLE.getMessage());
        }
    }

    private void validateUpdatable(User user, Asset asset) {
        boolean isSameLocation = user.getLocation().equals(asset.getLocation());
        if (!isSameLocation) {
            log.warn("User {} is not allowed to edit asset {} due to location mismatch", user.getUsername(), asset.getId());
            throw new AssetNotEditableException(ErrorCode.USER_AND_ASSET_LOCATION_MISMATCH.getMessage());
        }

        validateAssetNotAssigned(asset);
    }

    private void validateAssetNotAssigned(Asset asset) {
        if (asset.getState() == AssetState.ASSIGNED) {
            log.warn("Asset with id {} is not available for update", asset.getId());
            throw new AssetNotEditableException(ErrorCode.ASSET_NOT_EDITABLE.getMessage());
        }
    }


    private Category findCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.debug("Category with id {} not found", categoryId);
                    return new CategoryNotFoundException(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
                });
    }

    private Asset buildUpdatedAssignment(Asset existing, UpdateAssetRequest request) {
        AssetState state = AssetState.valueOf(request.getState().toUpperCase());

        return existing.toBuilder()
                .state(state)
                .name(request.getName())
                .specification(request.getSpecification())
                .installedDate(request.getInstalledDate())
                .version(request.getVersion())
                .build();
    }
}
