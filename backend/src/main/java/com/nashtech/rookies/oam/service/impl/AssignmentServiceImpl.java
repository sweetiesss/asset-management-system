package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssignmentPageRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentUpdateRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssignmentStatusRequest;
import com.nashtech.rookies.oam.dto.response.AssignmentDetailResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentEditViewResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentPageResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentResponse;
import com.nashtech.rookies.oam.exception.*;
import com.nashtech.rookies.oam.mapper.AssignmentMapper;
import com.nashtech.rookies.oam.model.*;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.projection.AssignmentEditViewProjection;
import com.nashtech.rookies.oam.repository.AssetReturnRepository;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.AssignmentStatusRepository;
import com.nashtech.rookies.oam.service.AssetService;
import com.nashtech.rookies.oam.service.AssignmentService;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.UserService;
import com.nashtech.rookies.oam.service.enums.AssignmentActionRule;
import com.nashtech.rookies.oam.specification.AssignmentSpecification;
import com.nashtech.rookies.oam.util.SortUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.nashtech.rookies.oam.model.enums.AssignmentStatusType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AuthService authService;
    private final AssetService assetService;
    private final UserService userService;

    private final AssignmentRepository assignmentRepository;
    private final AssignmentStatusRepository assignmentStatusRepository;

    private final AssignmentMapper assignmentMapper;

    private AssignmentStatus waitingForAcceptanceStatus;
    private final AssetReturnRepository assetReturnRepository;

    @PostConstruct
    public void cacheStatuses() {
        this.waitingForAcceptanceStatus = assignmentStatusRepository.findByName(AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName())
                .orElseThrow(() -> new AssignmentStatusNotFoundException(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        User user = userService.getUserById(request.getUserId());
        Asset asset = assetService.getAssetByIdForUpdate(request.getAssetId());

        validateAssetAvailability(asset);

        Assignment assignment = assignmentMapper.toEntity(request, user, asset, waitingForAcceptanceStatus);
        assignment.setStatus(waitingForAcceptanceStatus);

        Assignment saved = assignmentRepository.save(assignment);
        assetService.updateAssetState(asset, AssetState.ASSIGNED);

        return assignmentMapper.toResponse(saved);
    }

    @Override
    public AssignmentEditViewResponse getAssignmentEditView(UUID id) {
        AssignmentEditViewProjection projection = assignmentRepository.findProjectedById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage()));

        return assignmentMapper.toEditViewResponse(projection);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(UUID id, AssignmentUpdateRequest request) throws BadRequestException {
        Assignment assignment = getAssignmentById(id);
        validateAssignmentState(assignment, AssignmentActionRule.UPDATE);
        validateRequestUpdate(assignment, request);

        Assignment updated = buildUpdatedAssignment(assignment, request);

        try {
            assignmentRepository.save(updated);
            logAssignmentUpdate(updated.getId());
            return assignmentMapper.toUpdatedResponse(updated);
        } catch (OptimisticLockingFailureException e) {
            log.error("Assignment with id {} is being modified by another transaction", id);
            throw new AssignmentBeingModifiedException(ErrorCode.ASSIGNMENT_BEING_MODIFIED.getMessage());
        }
    }

    @Transactional
    @Override
    @PreAuthorize("@assignmentSecurity.isCurrentUserAssigned(#id)")
    public AssignmentResponse updateAssignmentStatus(UUID id, UpdateAssignmentStatusRequest request){
        Assignment assignment = getAssignmentById(id);
        validateAssignmentState(assignment, AssignmentActionRule.UPDATE);

        AssignmentStatus status = getAssignmentStatus(AssignmentStatusType.valueOf(request.getStatus()));
        assignment.setStatus(status);

        if (request.getStatus().equals(AssignmentStatusType.DECLINED.getDbName())) {
            assetService.updateAssetState(assignment.getAsset(), AssetState.AVAILABLE);
        }

        Assignment persisted = assignmentRepository.save(assignment);
        logAssignmentUpdate(assignment.getId());
        return assignmentMapper.toResponse(persisted);
    }


    @Override
    public Assignment getAssignmentById(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public void deleteAssignment(UUID id) {
        Assignment assignment = loadAssignmentForDelete(id);
        validateAssignmentState(assignment, AssignmentActionRule.DELETE);
        releaseAssetInAssignment(assignment);
        performDelete(assignment, id);
    }

    @Override
    public APIPageableResponseDTO<AssignmentPageResponse> getAssignments(
            AssignmentPageRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();

        if(!isUserAdmin(currentUser)) {
            if (request.getUserId() != null){
                checkUserAccessPermission(currentUser, request.getUserId());
                request.setStates(getStaffAssignmentStates());
            }
            else
                request.setUserId(currentUser.getId());
        }

        if (CollectionUtils.isEmpty(request.getStates())) {
            request.setStates(getDefaultAssignmentStates());
        }

        Sort sort = SortUtil.buildAssignmentSort(request.getSort(), request.getSortOrder());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        UUID userId = request.getUserId();

        Specification<Assignment> spec = AssignmentSpecification.build(
                request.getSearch(),
                request.getStates(),
                request.getAssignedDateFrom(),
                request.getAssignedDateTo(),
                userId,
                currentUser.getLocation().getId()
        );


        Page<Assignment> assignmentPage = assignmentRepository.findAll(spec, pageable);

        Page<AssignmentPageResponse> dtoPage = assignmentPage.map(assignment -> {
            Optional<AssetReturn> latestReturn = assetReturnRepository
                    .findTopByAssignmentOrderByCreatedAtDesc(assignment);

            ReturnState latestState = latestReturn.map(AssetReturn::getState).orElse(null);

            return assignmentMapper.toAssignmentPageResponse(assignment, latestState);
        });

        return new APIPageableResponseDTO<>(dtoPage);
    }

    private void validateAssetAvailability(Asset asset) {
        if (!assetService.isAssetAvailable(asset)) {
            throw new AssetNotAvailableException(ErrorCode.ASSET_NOT_AVAILABLE.getMessage());
        }
    }

    private List<String> getStaffAssignmentStates() {
        return List.of(ACCEPTED.getDbName(), WAITING_FOR_ACCEPTANCE.getDbName());
    }

    private void checkUserAccessPermission(User currentUser, UUID userId) {
        if ( !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException(ErrorCode.USER_ASSIGNMENT_ACCESS_DENIED.getMessage());
        }
    }

    private boolean isUserAdmin(User currentUser) {
        return currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleName.ADMIN.getName()));
    }

    @Override
    public AssignmentDetailResponse getAssignmentDetail(UUID id) {
        Assignment assignment = assignmentRepository.findWithAssetAndUserById(id)
                .orElseThrow(() -> new AssignmentNotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage()));

        return assignmentMapper.toDetailResponse(assignment);
    }

    private void validateRequestUpdate(Assignment existing, AssignmentUpdateRequest request) throws BadRequestException {
        LocalDate newDate = request.getAssignedDate();
        LocalDate oldDate = existing.getAssignedDate();
        LocalDate today = LocalDate.now();

        if (newDate.isBefore(oldDate) && newDate.isBefore(today)) {
            throw new BadRequestException(ErrorCode.ASSIGNMENT_DATE_UPDATE_INVALID.getMessage());
        }
    }

    private void validateAssignmentState(Assignment assignment, AssignmentActionRule rule) {
        if (!isAssignmentWaitingForAcceptance(assignment)) {
            log.warn("Assignment {} is not {}", assignment.getId(), rule.getAction());
            throw rule.getException();
        }
    }

    private List<String> getDefaultAssignmentStates() {
        return List.of(ACCEPTED.getDbName(), WAITING_FOR_ACCEPTANCE.getDbName(), DECLINED.getDbName());
    }


    private Asset fetchAndValidateAssetForUpdate(Assignment assignment, UUID requestedAssetId) {
        try {
            Asset requestedAsset = assetService.getAssetByIdForUpdate(requestedAssetId);

            if (isSameAssetDuringWaitingStatus(assignment, requestedAsset) || assetService.isAssetAvailable(requestedAsset)) {
                return requestedAsset;
            }

            throw new AssetNotAvailableException(ErrorCode.ASSET_NOT_AVAILABLE.getMessage());
        } catch (OptimisticLockingFailureException e) {
            log.error("Asset with id {} is being modified by another transaction", requestedAssetId);
            throw new AssetNotAvailableException(ErrorCode.ASSET_NOT_AVAILABLE.getMessage());
        }
    }

    private boolean isSameAssetDuringWaitingStatus(Assignment assignment, Asset asset) {
        return assignment.getAsset().getId().equals(asset.getId()) &&
                isAssignmentWaitingForAcceptance(assignment);
    }

    private Assignment buildUpdatedAssignment(Assignment existing, AssignmentUpdateRequest request) {
        User newUser = userService.getUserById(request.getUserId());
        Asset newAsset = fetchAndValidateAssetForUpdate(existing, request.getAssetId());

        releaseAssetInAssignment(existing);
        assetService.updateAssetState(newAsset, AssetState.ASSIGNED);

        Assignment updatedAssignment = existing.toBuilder()
                .user(newUser)
                .asset(newAsset)
                .assignedDate(request.getAssignedDate())
                .note(request.getNote())
                .version(request.getVersion())
                .build();

        updatedAssignment.setUpdatedBy(authService.getAuthenticatedUser().getUsername());
        return updatedAssignment;
    }


    private void logAssignmentUpdate(UUID assignmentId) {
        String currentUsername = authService.getAuthenticatedUser().getUsername();
        log.info("User {} is updating assignment {}", currentUsername, assignmentId);
    }

    private boolean isAssignmentWaitingForAcceptance(Assignment assignment) {
        return Objects.equals(assignment.getStatus().getName(), AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName());
    }

    private Assignment loadAssignmentForDelete(UUID id) {
        return assignmentRepository.getAssignmentByIdForDelete(id)
                .orElseThrow(() -> new AssignmentAlreadyDeletedException(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage()));
    }

    private void releaseAssetInAssignment(Assignment assignment) {
        assetService.updateAssetState(assignment.getAsset(), AssetState.AVAILABLE);
    }

    private void performDelete(Assignment assignment, UUID id) {
        assignmentRepository.delete(assignment);
        logUserDeletionAction(id);
    }

    private void logUserDeletionAction(UUID id) {
        User currentUser = authService.getAuthenticatedUser();
        log.info("User {} is deleting assignment {}", currentUser.getUsername(), id);
    }

    private AssignmentStatus getAssignmentStatus (AssignmentStatusType status) {
        return assignmentStatusRepository.findByName(status.getDbName())
                .orElseThrow(() -> new AssignmentStatusNotFoundException(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND.getMessage()));
    }

    @Override
    public List<AssignmentStatus> getAllAssignmentStatus() {
        return assignmentStatusRepository.findAll();
    }

}
