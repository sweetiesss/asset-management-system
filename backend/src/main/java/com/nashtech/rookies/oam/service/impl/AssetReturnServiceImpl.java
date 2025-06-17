package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetReturnPageRequest;
import com.nashtech.rookies.oam.dto.request.AssetReturnRequest;
import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetReturnResponse;
import com.nashtech.rookies.oam.exception.*;
import com.nashtech.rookies.oam.mapper.AssetReturnMapper;
import com.nashtech.rookies.oam.model.AssetReturn;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.repository.AssetReturnRepository;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.AssignmentStatusRepository;
import com.nashtech.rookies.oam.service.AssetReturnService;
import com.nashtech.rookies.oam.service.AssetService;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.specification.AssetReturnSpecification;
import com.nashtech.rookies.oam.util.SortUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssetReturnServiceImpl implements AssetReturnService {
    private final AssignmentRepository assignmentRepository;
    private final AssetReturnRepository assetReturnRepository;
    private final AssignmentStatusRepository assignmentStatusRepository;
    private final AssetReturnMapper assetReturnMapper;
    private final AuthService authService;
    private final Clock clock;
    private final AssetService assetService;

    @Override
    @Transactional
    public AssetReturnResponse createAssetReturn(UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage()));

        User currentUser = authService.getAuthenticatedUser();

        checkUserPermission(assignment, currentUser);

        AssignmentStatusType statusType = AssignmentStatusType
                .fromDbName(assignment.getStatus().getName())
                .orElseThrow(() -> new AssignmentStatusNotFoundException(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND.getMessage()));


        if (!AssignmentStatusType.ACCEPTED.equals(statusType)) {
            throw new AssignmentNotAcceptedException(ErrorCode.ASSIGNMENT_NOT_ACCEPTED.getMessage());
        }

        checkExistingReturnRequest(assignment);

        AssetReturn assetReturn = initializeAssetReturn(assignment);
        assetReturn = assetReturnRepository.save(assetReturn);

        return assetReturnMapper.toDTO(assetReturn);
    }

    @Override
    @Transactional
    public AssetReturnResponse updateAssetReturn(UUID returnId, AssetReturnRequest request) {
        AssetReturn assetReturn = getAssetReturnById(returnId);
        validateUpdatable(assetReturn);

        updateAssetReturnState(assetReturn, request);

        return assetReturnMapper.toDTO(assetReturn);
    }

    private void updateAssetReturnState(AssetReturn assetReturn, AssetReturnRequest request) {
        Assignment assignment = getAssignmentFromReturn(assetReturn);
        ReturnState newState = parseReturnState(request.getState());

        applyStateTransition(assetReturn, assignment, newState);
        saveEntities(assetReturn, assignment);

        log.info("Updated asset return and assignment for ID: {}", assetReturn.getId());
    }

    private Assignment getAssignmentFromReturn(AssetReturn assetReturn) {
        return Optional.ofNullable(assetReturn.getAssignment())
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found for asset return"));
    }

    private ReturnState parseReturnState(String stateString) {
        try {
            return ReturnState.valueOf(stateString);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestReturnStateException(ErrorCode.INVALID_ASSET_RETURN_STATE.getMessage());
        }
    }

    private void applyStateTransition(AssetReturn assetReturn, Assignment assignment, ReturnState newState) {
        switch (newState) {
            case COMPLETED -> handleCompletedState(assetReturn, assignment);
            case CANCELED -> handleCanceledState(assetReturn, assignment);
            default -> throw new InvalidRequestReturnStateException(ErrorCode.INVALID_ASSET_RETURN_STATE.getMessage());
        }
    }

    private void handleCompletedState(AssetReturn assetReturn, Assignment assignment) {
        assignment.setStatus(getAssignmentStatus(AssignmentStatusType.COMPLETED));
        assetReturn.setReturnedDate(LocalDate.now(clock));
        assetReturn.setState(ReturnState.COMPLETED);
        assetService.updateAssetState(assignment.getAsset(), AssetState.AVAILABLE);
    }

    private void handleCanceledState(AssetReturn assetReturn, Assignment assignment) {
        assignment.setStatus(getAssignmentStatus(AssignmentStatusType.ACCEPTED));
        assetReturn.setState(ReturnState.CANCELED);
    }

    private void saveEntities(AssetReturn assetReturn, Assignment assignment) {
        try {
            assignmentRepository.save(assignment);
            assetReturnRepository.save(assetReturn);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent modification detected for asset return: {}", assetReturn.getId());
            throw new ReturnAssetBeingModifiedException(ErrorCode.RETURN_ASSET_BEING_MODIFIED.getMessage());
        }
    }

    @Override
    public APIPageableResponseDTO<AssetReturnPageResponse> getAssetReturns(AssetReturnPageRequest request) {
        if (CollectionUtils.isEmpty(request.getStates())) {
            request.setStates(getDefaultReturnStates());
        }

        Sort sort = SortUtil.buildAssetReturnSort(request.getSort(), request.getSortOrder());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        User currentUser = authService.getAuthenticatedUser();

        Specification<AssetReturn> spec = AssetReturnSpecification.build(
                request.getSearch(),
                request.getStates(),
                request.getReturnedDateFrom(),
                request.getReturnedDateTo(),
                currentUser.getLocation().getId()
        );

        Page<AssetReturn> page = assetReturnRepository.findAll(spec, pageable);
        Page<AssetReturnPageResponse> dtoPage = page.map(assetReturnMapper::toAssetReturnPageResponse);

        return new APIPageableResponseDTO<>(dtoPage);
    }

    private List<String> getDefaultReturnStates() {
        return List.of(ReturnState.WAITING_FOR_RETURNING.name(), ReturnState.COMPLETED.name());
    }


    private void checkUserPermission(Assignment assignment, User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> RoleName.ADMIN.getName().equals(role.getName()));

        if (!isAdmin && !assignment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorCode.FORBIDDEN.getMessage());
        }
    }

    private void checkExistingReturnRequest(Assignment assignment) {
        assetReturnRepository.findTopByAssignmentOrderByCreatedAtDesc(assignment)
                .ifPresent(latest -> {
                    switch (latest.getState()) {
                        case WAITING_FOR_RETURNING ->
                                throw new RequestReturnAssetAlreadyExistsException(ErrorCode.REQUEST_RETURN_ASSET_ALREADY_EXISTS.getMessage());
                        case COMPLETED ->
                                throw new AssetAlreadyReturnedException(ErrorCode.ASSET_ALREADY_RETURNED.getMessage());
                        case CANCELED -> {
                        }
                        default -> throw new AssetReturnStateInvalidException(ErrorCode.ASSET_RETURN_STATE_INVALID.getMessage());
                    }
                });
    }

    private AssetReturn initializeAssetReturn(Assignment assignment) {
        return AssetReturn.builder()
                .assignment(assignment)
                .state(ReturnState.WAITING_FOR_RETURNING)
                .version(0L)
                .build();
    }

    private AssignmentStatus getAssignmentStatus (AssignmentStatusType status) {
        return assignmentStatusRepository.findByName(status.getDbName())
                .orElseThrow(() -> new AssignmentStatusNotFoundException(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND.getMessage()));
    }

    private AssetReturn getAssetReturnById(UUID returnId) {
        return assetReturnRepository.findById(returnId).orElseThrow(
                () -> new RequestReturnNotFoundException(ErrorCode.REQUEST_RETURN_NOT_FOUND.getMessage())
        );
    }

    private void validateUpdatable(AssetReturn assetReturn) {
        if(!isAssetReturnWaitingForReturning(assetReturn)){
            log.warn("Asset return {} is not updatable", assetReturn.getId());
            throw new AssetReturnRequestNotUpdatableException(ErrorCode.ASSET_RETURN_NOT_UPDATABLE.getMessage());
        }
    }

    private boolean isAssetReturnWaitingForReturning(AssetReturn assetReturn) {
        return Objects.equals(assetReturn.getState().name(), ReturnState.WAITING_FOR_RETURNING.name());
    }
}
