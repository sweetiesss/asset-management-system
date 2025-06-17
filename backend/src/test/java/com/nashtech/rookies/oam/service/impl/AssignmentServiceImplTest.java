package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssignmentPageRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.request.AssignmentUpdateRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssignmentStatusRequest;
import com.nashtech.rookies.oam.dto.response.AssignmentEditViewResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentPageResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentResponse;
import com.nashtech.rookies.oam.exception.*;
import com.nashtech.rookies.oam.mapper.AssignmentMapper;
import com.nashtech.rookies.oam.model.*;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.projection.AssignmentEditViewProjection;
import com.nashtech.rookies.oam.repository.AssetReturnRepository;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.AssignmentStatusRepository;
import com.nashtech.rookies.oam.service.AssetService;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.UserService;
import com.nashtech.rookies.oam.service.enums.AssignmentActionRule;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private AuthService authService;

    @Mock
    private AssetService assetService;

    @Mock
    private UserService userService;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentStatusRepository assignmentStatusRepository;

    @Mock
    private AssignmentMapper assignmentMapper;

    @Mock
    private AssetReturnRepository assetReturnRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @Captor
    private ArgumentCaptor<Assignment> assignmentCaptor;

    private AssignmentRequest assignmentRequest;
    private User user;
    private Asset asset;
    private AssignmentStatus waitingForAcceptanceStatus;
    private Assignment assignmentEntity;
    private Assignment savedAssignment;
    private AssignmentResponse assignmentResponse;
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        assignmentRequest = new AssignmentRequest();
        assignmentRequest.setUserId(userId);
        assignmentRequest.setAssetId(assetId);
        assignmentRequest.setAssignedDate(LocalDate.now().plusDays(1));
        assignmentRequest.setNote("Sample note");

        user = User.builder()
                .id(userId)
                .staffCode("SD0001")
                .username("testuser")
                .build();

        asset = Asset.builder()
                .id(assetId)
                .code("ASSET001")
                .name("Test Asset")
                .state(AssetState.AVAILABLE)
                .build();

        waitingForAcceptanceStatus = AssignmentStatus.builder()
                .id(1)
                .name(AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName())
                .build();

        assignmentEntity = Assignment.builder()
                .note(assignmentRequest.getNote())
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(assignmentRequest.getAssignedDate())
                .build();

        savedAssignment = Assignment.builder()
                .id(UUID.randomUUID())
                .note(assignmentRequest.getNote())
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(assignmentRequest.getAssignedDate())
                .build();

        assignmentResponse = AssignmentResponse.builder()
                .id(savedAssignment.getId())
                .assetCode(asset.getCode())
                .assetName(asset.getName())
                .assignTo(user.getUsername())
                .assignBy("creatorUser")
                .assignedDate(savedAssignment.getAssignedDate())
                .state(waitingForAcceptanceStatus)
                .build();

        authenticatedUser = User.builder()
                .id(UUID.randomUUID())
                .username("authenticatedUser")
                .build();

        // Set up the cached status
        ReflectionTestUtils.setField(
                assignmentService,
                "waitingForAcceptanceStatus",
                waitingForAcceptanceStatus
        );
    }

    // Test for cacheStatuses method
    @Test
    void cacheStatuses_WhenStatusExists_ShouldCacheSuccessfully() {
        AssignmentServiceImpl newService = new AssignmentServiceImpl(
                authService, assetService, userService, assignmentRepository,
                assignmentStatusRepository, assignmentMapper, assetReturnRepository
        );

        when(assignmentStatusRepository.findByName(AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName()))
                .thenReturn(Optional.of(waitingForAcceptanceStatus));

        assertDoesNotThrow(() -> newService.cacheStatuses());

        verify(assignmentStatusRepository).findByName(AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName());
    }

    @Test
    void cacheStatuses_WhenStatusNotFound_ShouldThrowException() {
        AssignmentServiceImpl newService = new AssignmentServiceImpl(
                authService, assetService, userService, assignmentRepository,
                assignmentStatusRepository, assignmentMapper, assetReturnRepository
        );

        when(assignmentStatusRepository.findByName(AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName()))
                .thenReturn(Optional.empty());

        AssignmentStatusNotFoundException exception = assertThrows(
                AssignmentStatusNotFoundException.class,
                () -> newService.cacheStatuses()
        );

        assertEquals(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void createAssignment_WithValidData_ShouldCreateSuccessfully() {

        asset.setState(AssetState.AVAILABLE);

        when(userService.getUserById(assignmentRequest.getUserId())).thenReturn(user);
        when(assetService.getAssetByIdForUpdate(assignmentRequest.getAssetId())).thenReturn(asset);
        when(assetService.isAssetAvailable(asset)).thenReturn(true);
        when(assignmentMapper.toEntity(assignmentRequest, user, asset, waitingForAcceptanceStatus)).thenReturn(assignmentEntity);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentMapper.toResponse(savedAssignment)).thenReturn(assignmentResponse);

        AssignmentResponse response = assignmentService.createAssignment(assignmentRequest);

        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment capturedAssignment = assignmentCaptor.getValue();

        assertEquals(waitingForAcceptanceStatus, capturedAssignment.getStatus());
        verify(assetService).updateAssetState(asset, AssetState.ASSIGNED);
        assertEquals(assignmentResponse, response);
    }

    @Test
    void createAssignment_WhenAssetNotAvailable_ShouldThrowException() {

        asset.setState(AssetState.ASSIGNED);

        when(userService.getUserById(assignmentRequest.getUserId())).thenReturn(user);
        when(assetService.getAssetByIdForUpdate(assignmentRequest.getAssetId())).thenReturn(asset);
        when(assetService.isAssetAvailable(asset)).thenReturn(false);

        AssetNotAvailableException exception = assertThrows(
                AssetNotAvailableException.class,
                () -> assignmentService.createAssignment(assignmentRequest)
        );

        assertEquals(ErrorCode.ASSET_NOT_AVAILABLE.getMessage(), exception.getMessage());

        verify(assignmentRepository, never()).save(any());
        verify(assetService, never()).updateAssetState(any(), any());
    }

    @Test
    void createAssignment_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        when(userService.getUserById(assignmentRequest.getUserId()))
                .thenThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> assignmentService.createAssignment(assignmentRequest));

        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        verifyNoInteractions(assetService, assignmentRepository, assignmentMapper);
    }

    @Test
    void createAssignment_WhenAssetNotFound_ShouldThrowAssetNotFoundException() {
        when(userService.getUserById(assignmentRequest.getUserId())).thenReturn(user);
        when(assetService.getAssetByIdForUpdate(assignmentRequest.getAssetId()))
                .thenThrow(new AssetNotFoundException(ErrorCode.ASSET_NOT_FOUND.getMessage()));

        AssetNotFoundException exception = assertThrows(AssetNotFoundException.class,
                () -> assignmentService.createAssignment(assignmentRequest));

        assertEquals(ErrorCode.ASSET_NOT_FOUND.getMessage(), exception.getMessage());
        verifyNoInteractions(assignmentRepository, assignmentMapper);
    }

    @Test
    void createAssignment_WhenAssetNotAvailable_ShouldThrowAssetNotAvailableException() {
        when(userService.getUserById(assignmentRequest.getUserId())).thenReturn(user);
        when(assetService.getAssetByIdForUpdate(assignmentRequest.getAssetId()))
                .thenThrow(new AssetNotAvailableException(ErrorCode.ASSET_NOT_AVAILABLE.getMessage()));

        AssetNotAvailableException exception = assertThrows(AssetNotAvailableException.class,
                () -> assignmentService.createAssignment(assignmentRequest));

        assertEquals(ErrorCode.ASSET_NOT_AVAILABLE.getMessage(), exception.getMessage());
        verifyNoInteractions(assignmentMapper, assignmentRepository);
    }

    @Test
    void createAssignment_WithOptimisticLockingFailure_ShouldThrowAssetNotAvailableException() {
        when(userService.getUserById(assignmentRequest.getUserId())).thenReturn(user);
        when(assetService.getAssetByIdForUpdate(assignmentRequest.getAssetId()))
                .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        OptimisticLockingFailureException exception = assertThrows(OptimisticLockingFailureException.class,
                () -> assignmentService.createAssignment(assignmentRequest));

        assertEquals("Concurrent modification", exception.getMessage());
        verifyNoInteractions(assignmentMapper, assignmentRepository);
    }

    @Test
    void getAssignmentEditView_WithValidId_ShouldReturnEditViewResponse() {
        UUID assignmentId = UUID.randomUUID();
        AssignmentEditViewProjection projection = mock(AssignmentEditViewProjection.class);
        AssignmentEditViewResponse expectedResponse = new AssignmentEditViewResponse();

        when(assignmentRepository.findProjectedById(assignmentId)).thenReturn(Optional.of(projection));
        when(assignmentMapper.toEditViewResponse(projection)).thenReturn(expectedResponse);

        AssignmentEditViewResponse response = assignmentService.getAssignmentEditView(assignmentId);

        assertEquals(expectedResponse, response);
        verify(assignmentRepository).findProjectedById(assignmentId);
        verify(assignmentMapper).toEditViewResponse(projection);
    }

    @Test
    void getAssignmentEditView_WithInvalidId_ShouldThrowAssignmentNotFoundException() {
        UUID assignmentId = UUID.randomUUID();

        when(assignmentRepository.findProjectedById(assignmentId)).thenReturn(Optional.empty());

        AssignmentNotFoundException exception = assertThrows(AssignmentNotFoundException.class,
                () -> assignmentService.getAssignmentEditView(assignmentId));

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void updateAssignment_WithValidData_ShouldUpdateSuccessfully() throws BadRequestException {
        UUID assignmentId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID newAssetId = UUID.randomUUID();
        LocalDate originalDate = LocalDate.now();
        LocalDate newDate = LocalDate.now().plusDays(1);

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(originalDate)
                .note("Old note")
                .build();

        User newUser = User.builder()
                .id(newUserId)
                .username("newuser")
                .build();

        Asset newAsset = Asset.builder()
                .id(newAssetId)
                .code("ASSET002")
                .name("New Asset")
                .state(AssetState.AVAILABLE)
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(newUserId);
        updateRequest.setAssetId(newAssetId);
        updateRequest.setAssignedDate(newDate);
        updateRequest.setNote("Updated note");

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(assetService.getAssetByIdForUpdate(newAssetId)).thenReturn(newAsset);
        when(assetService.isAssetAvailable(newAsset)).thenReturn(true);
        when(userService.getUserById(newUserId)).thenReturn(newUser);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(existingAssignment);
        when(assignmentMapper.toUpdatedResponse(existingAssignment)).thenReturn(assignmentResponse);
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, updateRequest);

        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment capturedAssignment = assignmentCaptor.getValue();

        assertEquals(newUser, capturedAssignment.getUser());
        assertEquals(newAsset, capturedAssignment.getAsset());
        assertEquals(newDate, capturedAssignment.getAssignedDate());
        assertEquals("Updated note", capturedAssignment.getNote());
        verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
        verify(assetService).updateAssetState(newAsset, AssetState.ASSIGNED);
        assertEquals(assignmentResponse, response);
    }

    @Test
    void updateAssignment_WithInvalidId_ShouldThrowAssignmentNotFoundException() {
        UUID assignmentId = UUID.randomUUID();
        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        AssignmentNotFoundException exception = assertThrows(AssignmentNotFoundException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void updateAssignment_WithNonUpdatableStatus_ShouldThrowAssignmentNotUpdatableException() {
        UUID assignmentId = UUID.randomUUID();

        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .id(2)
                .name("Accepted")
                .build();

        Assignment nonUpdatableAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(acceptedStatus)
                .assignedDate(LocalDate.now())
                .note("Note")
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(UUID.randomUUID());
        updateRequest.setAssetId(UUID.randomUUID());
        updateRequest.setAssignedDate(LocalDate.now());

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(nonUpdatableAssignment));

        AssignmentNotUpdatableException exception = assertThrows(AssignmentNotUpdatableException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals(ErrorCode.ASSIGNMENT_NOT_UPDATABLE.getMessage(), exception.getMessage());
    }

    @Test
    void updateAssignment_WithInvalidUserId_ShouldThrowUserNotFoundException() {
        UUID assignmentId = UUID.randomUUID();
        UUID invalidUserId = UUID.randomUUID();
        LocalDate originalDate = LocalDate.now();

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(originalDate)
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(invalidUserId);
        updateRequest.setAssetId(UUID.randomUUID());
        updateRequest.setAssignedDate(originalDate.plusDays(1));

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(userService.getUserById(invalidUserId))
                .thenThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void updateAssignment_WithSameAssetDuringWaitingStatus_ShouldUpdateSuccessfully() throws BadRequestException {
        UUID assignmentId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        LocalDate originalDate = LocalDate.now();
        LocalDate newDate = LocalDate.now().plusDays(1);

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(originalDate)
                .note("Old note")
                .build();

        User newUser = User.builder()
                .id(newUserId)
                .username("newuser")
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(newUserId);
        updateRequest.setAssetId(asset.getId()); // Same asset ID
        updateRequest.setAssignedDate(newDate);
        updateRequest.setNote("Updated note");

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(assetService.getAssetByIdForUpdate(asset.getId())).thenReturn(asset);
        when(userService.getUserById(newUserId)).thenReturn(newUser);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(existingAssignment);
        when(assignmentMapper.toUpdatedResponse(existingAssignment)).thenReturn(assignmentResponse);
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, updateRequest);

        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment capturedAssignment = assignmentCaptor.getValue();

        assertEquals(newUser, capturedAssignment.getUser());
        assertEquals(asset, capturedAssignment.getAsset());
        assertEquals(newDate, capturedAssignment.getAssignedDate());
        assertEquals("Updated note", capturedAssignment.getNote());
        // When same asset, should still update asset states
        verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
        verify(assetService).updateAssetState(asset, AssetState.ASSIGNED);
        assertEquals(assignmentResponse, response);
    }

    @Test
    void updateAssignment_WithUnavailableAsset_ShouldThrowAssetNotAvailableException() {
        UUID assignmentId = UUID.randomUUID();
        UUID newAssetId = UUID.randomUUID();
        LocalDate originalDate = LocalDate.now();

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(originalDate)
                .build();

        Asset unavailableAsset = Asset.builder()
                .id(newAssetId)
                .state(AssetState.NOT_AVAILABLE)
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(user.getId());
        updateRequest.setAssetId(newAssetId);
        updateRequest.setAssignedDate(originalDate);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(assetService.getAssetByIdForUpdate(newAssetId)).thenReturn(unavailableAsset);
        when(assetService.isAssetAvailable(unavailableAsset)).thenReturn(false);

        AssetNotAvailableException exception = assertThrows(AssetNotAvailableException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals(ErrorCode.ASSET_NOT_AVAILABLE.getMessage(), exception.getMessage());
    }

    @Test
    void updateAssignment_WithOptimisticLockingFailure_ShouldThrowAssetNotAvailableException() {
        UUID assignmentId = UUID.randomUUID();
        UUID newAssetId = UUID.randomUUID();
        LocalDate originalDate = LocalDate.now();

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(originalDate)
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(user.getId());
        updateRequest.setAssetId(newAssetId);
        updateRequest.setAssignedDate(originalDate);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(assetService.getAssetByIdForUpdate(newAssetId))
                .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        AssetNotAvailableException exception = assertThrows(AssetNotAvailableException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals(ErrorCode.ASSET_NOT_AVAILABLE.getMessage(), exception.getMessage());
    }

    @Test
    void updateAssignment_WithEarlierAssignedDate_ShouldThrowBadRequestException() {
        UUID assignmentId = UUID.randomUUID();
        LocalDate originalDate = LocalDate.now();
        LocalDate earlierDate = originalDate.minusDays(1);

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(originalDate)
                .build();

        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
        updateRequest.setUserId(user.getId());
        updateRequest.setAssetId(asset.getId());
        updateRequest.setAssignedDate(earlierDate);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> assignmentService.updateAssignment(assignmentId, updateRequest));

        assertEquals("The updated Assigned Date must be later than the original Assigned Date", exception.getMessage());
    }

    @Test
    void isAssignmentWaitingForAcceptance_WithWaitingStatus_ShouldReturnTrue() {
        Assignment assignment = Assignment.builder()
                .status(waitingForAcceptanceStatus)
                .build();

        // Using reflection to test private method
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "isAssignmentWaitingForAcceptance",
                assignment
        );

        assertTrue(result);
    }

    @Test
    void isAssignmentWaitingForAcceptance_WithNonWaitingStatus_ShouldReturnFalse() {
        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .name("Accepted")
                .build();

        Assignment assignment = Assignment.builder()
                .status(acceptedStatus)
                .build();

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "isAssignmentWaitingForAcceptance",
                assignment
        );

        assertFalse(result);
    }

    @Test
    void validateUpdatable_WithWaitingStatus_ShouldNotThrowException() {
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .status(waitingForAcceptanceStatus)
                .build();

        // Using reflection to test private method
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                assignmentService,
                "validateAssignmentState",
                assignment,
                AssignmentActionRule.UPDATE
        ));
    }

    @Test
    void validateUpdatable_WithNonWaitingStatus_ShouldThrowException() {
        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .name("Accepted")
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .status(acceptedStatus)
                .build();

        AssignmentNotUpdatableException exception = assertThrows(
                AssignmentNotUpdatableException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        assignmentService,
                        "validateAssignmentState",
                        assignment,
                        AssignmentActionRule.UPDATE
                )
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_UPDATABLE.getMessage(), exception.getMessage());
    }

    @Test
    void fetchAndValidateAssetForUpdate_WithAvailableAsset_ShouldReturnAsset() {
        UUID assetId = UUID.randomUUID();
        Asset availableAsset = Asset.builder()
                .id(assetId)
                .state(AssetState.AVAILABLE)
                .build();

        Assignment assignment = Assignment.builder()
                .asset(asset) // Different asset
                .status(waitingForAcceptanceStatus)
                .build();

        when(assetService.getAssetByIdForUpdate(assetId)).thenReturn(availableAsset);
        when(assetService.isAssetAvailable(availableAsset)).thenReturn(true);

        Asset result = (Asset) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "fetchAndValidateAssetForUpdate",
                assignment,
                assetId
        );

        assertEquals(availableAsset, result);
    }

    @Test
    void fetchAndValidateAssetForUpdate_WithSameAssetDuringWaiting_ShouldReturnAsset() {
        Assignment assignment = Assignment.builder()
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .build();

        when(assetService.getAssetByIdForUpdate(asset.getId())).thenReturn(asset);

        Asset result = (Asset) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "fetchAndValidateAssetForUpdate",
                assignment,
                asset.getId()
        );

        assertEquals(asset, result);
    }

    @Test
    void fetchAndValidateAssetForUpdate_WithUnavailableAsset_ShouldThrowException() {
        UUID assetId = UUID.randomUUID();
        Asset unavailableAsset = Asset.builder()
                .id(assetId)
                .state(AssetState.NOT_AVAILABLE)
                .build();

        Assignment assignment = Assignment.builder()
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .build();

        when(assetService.getAssetByIdForUpdate(assetId)).thenReturn(unavailableAsset);
        when(assetService.isAssetAvailable(unavailableAsset)).thenReturn(false);

        AssetNotAvailableException exception = assertThrows(
                AssetNotAvailableException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        assignmentService,
                        "fetchAndValidateAssetForUpdate",
                        assignment,
                        assetId
                )
        );

        assertEquals(ErrorCode.ASSET_NOT_AVAILABLE.getMessage(), exception.getMessage());
    }

    @Test
    void isSameAssetDuringWaitingStatus_WithSameAssetAndWaitingStatus_ShouldReturnTrue() {
        Assignment assignment = Assignment.builder()
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .build();

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "isSameAssetDuringWaitingStatus",
                assignment,
                asset
        );

        assertTrue(result);
    }

    @Test
    void isSameAssetDuringWaitingStatus_WithDifferentAsset_ShouldReturnFalse() {
        Asset differentAsset = Asset.builder()
                .id(UUID.randomUUID())
                .build();

        Assignment assignment = Assignment.builder()
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .build();

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "isSameAssetDuringWaitingStatus",
                assignment,
                differentAsset
        );

        assertFalse(result);
    }

    @Test
    void logAssignmentUpdate_ShouldCallAuthService() {
        UUID assignmentId = UUID.randomUUID();

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        ReflectionTestUtils.invokeMethod(
                assignmentService,
                "logAssignmentUpdate",
                assignmentId

        );

        verify(authService).getAuthenticatedUser();
    }


    @Test
    void getAssignments_withValidRequest_shouldReturnPaginatedResponse() {
        AssignmentPageRequest request = new AssignmentPageRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSort("assetCode");
        request.setSortOrder("asc");
        request.setSearch("Dell");
        request.setStates(List.of("Accepted"));
        request.setAssignedDateTo(LocalDate.of(2025, 6, 2));

        Role adminRole = Role.builder()
                .name(RoleName.ADMIN.getName())
                .build();

        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .roles(Set.of(adminRole))
                .location(new Location(UUID.randomUUID(), "HCM", "Ho Chi Minh"))
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(adminUser);

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .user(user)
                .status(waitingForAcceptanceStatus)
                .assignedDate(request.getAssignedDateTo())
                .build();

        Page<Assignment> assignmentPage = new PageImpl<>(List.of(assignment));
        AssignmentPageResponse responseDTO = AssignmentPageResponse.builder()
                .id(assignment.getId())
                .assetCode("ASSET001")
                .assetName("Test Asset")
                .userId(user.getId().toString())
                .createdBy("admin")
                .assignedDate(request.getAssignedDateTo())
                .status(waitingForAcceptanceStatus)
                .build();

        when(assignmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(assignmentPage);

        when(assetReturnRepository.findTopByAssignmentOrderByCreatedAtDesc(eq(assignment)))
                .thenReturn(Optional.empty());


        when(assignmentMapper.toAssignmentPageResponse(eq(assignment), isNull()))
                .thenReturn(responseDTO);





        APIPageableResponseDTO<AssignmentPageResponse> result = assignmentService.getAssignments(request);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals("ASSET001", result.getContent().getFirst().getAssetCode());
        assertEquals("Test Asset", result.getContent().getFirst().getAssetName());
        assertEquals("admin", result.getContent().getFirst().getCreatedBy());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(1, result.getPageable().getPageSize());

        verify(assignmentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(assignmentMapper, times(1)).toAssignmentPageResponse(eq(assignment), isNull());

    }
    @Test
    void getAssignments_withAdminUser_shouldReturnPaginatedResponseForAllUsers() {
        // Arrange
        AssignmentPageRequest request = new AssignmentPageRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSort("assetCode");
        request.setSortOrder("asc");
        request.setSearch("Dell");
        request.setStates(List.of("Accepted"));
        request.setAssignedDateTo(LocalDate.of(2025, 6, 2));
        request.setUserId(UUID.randomUUID()); // Admin requesting specific user's data

        Role adminRole = Role.builder()
                .name(RoleName.ADMIN.getName())
                .build();

        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .roles(Set.of(adminRole))
                .location(new Location(UUID.randomUUID(), "HCM", "Ho Chi Minh"))
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .user(user)
                .status(waitingForAcceptanceStatus)
                .assignedDate(request.getAssignedDateTo())
                .build();

        Page<Assignment> assignmentPage = new PageImpl<>(List.of(assignment));
        AssignmentPageResponse responseDTO = AssignmentPageResponse.builder()
                .id(assignment.getId())
                .assetCode("ASSET001")
                .assetName("Test Asset")
                .userId(user.getId().toString())
                .createdBy("admin")
                .assignedDate(request.getAssignedDateTo())
                .status(waitingForAcceptanceStatus)
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(assignmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(assignmentPage);
        when(assetReturnRepository.findTopByAssignmentOrderByCreatedAtDesc(eq(assignment)))
                .thenReturn(Optional.empty());


        when(assignmentMapper.toAssignmentPageResponse(eq(assignment), isNull()))
                .thenReturn(responseDTO);

        APIPageableResponseDTO<AssignmentPageResponse> result = assignmentService.getAssignments(request);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals("ASSET001", result.getContent().getFirst().getAssetCode());
        assertEquals("Test Asset", result.getContent().getFirst().getAssetName());
        assertEquals("admin", result.getContent().getFirst().getCreatedBy());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(1, result.getPageable().getPageSize());

        verify(authService, times(1)).getAuthenticatedUser();
        verify(assignmentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(assignmentMapper, times(1)).toAssignmentPageResponse(eq(assignment), isNull());
    }

    @Test
    void getAssignments_withRegularUserAccessingOtherUserAssignments_shouldThrowAccessDeniedException() {
        // Arrange
        AssignmentPageRequest request = new AssignmentPageRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSort("assetCode");
        request.setSortOrder("asc");
        request.setSearch("Dell");
        request.setStates(List.of("Accepted"));
        request.setAssignedDateTo(LocalDate.of(2025, 6, 2));

        UUID otherUserId = UUID.randomUUID(); // different userId
        request.setUserId(otherUserId);

        // Logged-in user
        Role staffRole = Role.builder()
                .name(RoleName.STAFF.getName())
                .build();

        User regularUser = User.builder()
                .id(UUID.randomUUID()) // This ID is different from otherUserId
                .username("regularUser")
                .roles(Set.of(staffRole))
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(regularUser);

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> assignmentService.getAssignments(request)
        );

        assertEquals("You are not authorized to access assignments of this user", exception.getMessage());

        // Verify behavior
        verify(authService, times(1)).getAuthenticatedUser();
        verifyNoInteractions(assignmentRepository);
        verifyNoInteractions(assignmentMapper);
    }

    @Test
    void getAssignments_withNullUserId_shouldHandleAppropriately() {
        // Arrange
        AssignmentPageRequest request = new AssignmentPageRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSort("assetCode");
        request.setSortOrder("asc");
        request.setUserId(null); // No specific user requested

        // Create admin user
        Role adminRole = Role.builder()
                .name(RoleName.ADMIN.getName())
                .build();

        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .roles(Set.of(adminRole))
                .location(new Location(UUID.randomUUID(), "HCM", "Ho Chi Minh"))
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .user(user)
                .status(waitingForAcceptanceStatus)
                .assignedDate(LocalDate.now())
                .build();

        Page<Assignment> assignmentPage = new PageImpl<>(List.of(assignment));
        AssignmentPageResponse responseDTO = AssignmentPageResponse.builder()
                .id(assignment.getId())
                .assetCode("ASSET001")
                .assetName("Test Asset")
                .userId(user.getId().toString())
                .createdBy("admin")
                .assignedDate(LocalDate.now())
                .status(waitingForAcceptanceStatus)
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(assignmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(assignmentPage);
        lenient().when(assignmentMapper.toAssignmentPageResponse(any(Assignment.class)))
                .thenReturn(responseDTO);

        // Act
        APIPageableResponseDTO<AssignmentPageResponse> result = assignmentService.getAssignments(request);

        // Assert
        assertNotNull(result);
        verify(authService, times(1)).getAuthenticatedUser();
        verify(assignmentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAssignments_withEmptyStates_shouldUseDefaultStates() {
        // Arrange
        AssignmentPageRequest request = new AssignmentPageRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSort("assetCode");
        request.setStates(null); // Empty states should trigger default

        Role adminRole = Role.builder()
                .name(RoleName.ADMIN.getName())
                .build();

        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .roles(Set.of(adminRole))
                .location(new Location(UUID.randomUUID(), "HCM", "Ho Chi Minh"))
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .user(user)
                .status(waitingForAcceptanceStatus)
                .assignedDate(LocalDate.now())
                .build();

        Page<Assignment> assignmentPage = new PageImpl<>(List.of(assignment));
        AssignmentPageResponse responseDTO = AssignmentPageResponse.builder()
                .id(assignment.getId())
                .assetCode("ASSET001")
                .assetName("Test Asset")
                .userId(user.getId().toString())
                .createdBy("admin")
                .assignedDate(LocalDate.now())
                .status(waitingForAcceptanceStatus)
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(adminUser);
        when(assignmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(assignmentPage);
        lenient().when(assignmentMapper.toAssignmentPageResponse(any(Assignment.class)))
                .thenReturn(responseDTO);

        // Act
        APIPageableResponseDTO<AssignmentPageResponse> result = assignmentService.getAssignments(request);

        // Assert
        assertNotNull(result);
        assertNotNull(request.getStates()); // Should be populated with default states
        verify(authService, times(1)).getAuthenticatedUser();
    }

    @Test
    @DisplayName("Should update assignment status successfully")
    void updateAssignmentStatus_WithValidData_ShouldUpdateSuccessfully() {
        UUID assignmentId = UUID.randomUUID();
        UpdateAssignmentStatusRequest statusRequest = new UpdateAssignmentStatusRequest();
        statusRequest.setStatus(AssignmentStatusType.ACCEPTED.name());

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .note("Sample note")
                .build();

        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .id(2)
                .name(AssignmentStatusType.ACCEPTED.getDbName())
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(authService.getAuthenticatedUser()).thenReturn(user); // User is assigned
        when(assignmentStatusRepository.findByName(AssignmentStatusType.ACCEPTED.getDbName()))
                .thenReturn(Optional.of(acceptedStatus));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(existingAssignment);
        when(assignmentMapper.toResponse(existingAssignment)).thenReturn(assignmentResponse);

        AssignmentResponse response = assignmentService.updateAssignmentStatus(assignmentId, statusRequest);

        verify(assignmentRepository).save(assignmentCaptor.capture());
        Assignment capturedAssignment = assignmentCaptor.getValue();

        assertEquals(acceptedStatus, capturedAssignment.getStatus());
        assertEquals(assignmentResponse, response);
        verify(authService).getAuthenticatedUser();
        verify(assignmentStatusRepository).findByName(AssignmentStatusType.ACCEPTED.getDbName());
        verify(assignmentMapper).toResponse(existingAssignment);
    }

    @Test
    @DisplayName("Should throw AssignmentNotFoundException when assignment not found")
    void updateAssignmentStatus_WithInvalidId_ShouldThrowAssignmentNotFoundException() {
        UUID assignmentId = UUID.randomUUID();
        UpdateAssignmentStatusRequest statusRequest = new UpdateAssignmentStatusRequest();
        statusRequest.setStatus(AssignmentStatusType.ACCEPTED.name());

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        AssignmentNotFoundException exception = assertThrows(
                AssignmentNotFoundException.class,
                () -> assignmentService.updateAssignmentStatus(assignmentId, statusRequest)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assignmentRepository).findById(assignmentId);
        verifyNoInteractions(authService, assignmentStatusRepository, assignmentMapper);
    }

    @Test
    @DisplayName("Should throw AssignmentNotUpdatableException when assignment is not in WAITING_FOR_ACCEPTANCE status")
    void updateAssignmentStatus_WithNonUpdatableStatus_ShouldThrowAssignmentNotUpdatableException() {
        UUID assignmentId = UUID.randomUUID();
        UpdateAssignmentStatusRequest statusRequest = new UpdateAssignmentStatusRequest();
        statusRequest.setStatus(AssignmentStatusType.ACCEPTED.name());

        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .id(2)
                .name(AssignmentStatusType.ACCEPTED.getDbName())
                .build();

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(acceptedStatus)
                .note("Sample note")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));

        AssignmentNotUpdatableException exception = assertThrows(
                AssignmentNotUpdatableException.class,
                () -> assignmentService.updateAssignmentStatus(assignmentId, statusRequest)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_UPDATABLE.getMessage(), exception.getMessage());
        verify(assignmentRepository).findById(assignmentId);
        verifyNoInteractions(assignmentStatusRepository, assignmentMapper, authService);
    }

    @Test
    @DisplayName("Should throw AssignmentNotFoundException when status not found")
    void updateAssignmentStatus_WithInvalidStatus_ShouldThrowAssignmentNotFoundException() {
        UUID assignmentId = UUID.randomUUID();
        UpdateAssignmentStatusRequest statusRequest = new UpdateAssignmentStatusRequest();
        statusRequest.setStatus(AssignmentStatusType.ACCEPTED.name());

        Assignment existingAssignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .note("Sample note")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existingAssignment));
        when(assignmentStatusRepository.findByName(AssignmentStatusType.ACCEPTED.getDbName()))
                .thenReturn(Optional.empty());

        AssignmentStatusNotFoundException exception = assertThrows(
                AssignmentStatusNotFoundException.class,
                () -> assignmentService.updateAssignmentStatus(assignmentId, statusRequest)
        );

        assertEquals(ErrorCode.ASSIGNMENT_STATUS_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assignmentRepository).findById(assignmentId);
        verify(assignmentStatusRepository).findByName(AssignmentStatusType.ACCEPTED.getDbName());
        verifyNoInteractions(assignmentMapper);
    }

    @Test
    @DisplayName("Should delete assignment successfully when assignment is waiting for acceptance")
    void shouldDeleteAssignmentSuccessfully() {
        UUID assignmentId = UUID.randomUUID();

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        assertDoesNotThrow(() -> assignmentService.deleteAssignment(assignmentId));

        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
        verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
        verify(assignmentRepository).delete(assignment);
        verify(authService).getAuthenticatedUser();
    }

    @Test
    @DisplayName("Should throw AssignmentAlreadyDeletedException when assignment not found")
    void shouldThrowAssignmentAlreadyDeletedExceptionWhenAssignmentNotFound() {
        UUID assignmentId = UUID.randomUUID();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.empty());

        AssignmentAlreadyDeletedException exception = assertThrows(
                AssignmentAlreadyDeletedException.class,
                () -> assignmentService.deleteAssignment(assignmentId)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
        verifyNoInteractions(assetService);
        verifyNoMoreInteractions(assignmentRepository);
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should throw AssignmentNotDeletableException when assignment is not waiting for acceptance")
    void shouldThrowAssignmentNotDeletableExceptionWhenAssignmentNotWaitingForAcceptance() {
        UUID assignmentId = UUID.randomUUID();

        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .id(2)
                .name("Accepted")
                .build();

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(acceptedStatus)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));

        AssignmentNotDeletableException exception = assertThrows(
                AssignmentNotDeletableException.class,
                () -> assignmentService.deleteAssignment(assignmentId)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_DELETABLE.getMessage(), exception.getMessage());
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
        verifyNoInteractions(assetService);
        verify(assignmentRepository, never()).delete((Assignment) any());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should throw AssignmentNotDeletableException when assignment is declined")
    void shouldThrowAssignmentNotDeletableExceptionWhenAssignmentDeclined() {
        UUID assignmentId = UUID.randomUUID();

        AssignmentStatus declinedStatus = AssignmentStatus.builder()
                .id(3)
                .name("Declined")
                .build();

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(declinedStatus)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));

        AssignmentNotDeletableException exception = assertThrows(
                AssignmentNotDeletableException.class,
                () -> assignmentService.deleteAssignment(assignmentId)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_DELETABLE.getMessage(), exception.getMessage());
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
        verifyNoInteractions(assetService);
        verify(assignmentRepository, never()).delete((Assignment) any());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should release asset state before deletion")
    void shouldReleaseAssetStateBeforeDeletion() {
        UUID assignmentId = UUID.randomUUID();

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        assignmentService.deleteAssignment(assignmentId);

        InOrder inOrder = inOrder(assetService, assignmentRepository);
        inOrder.verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
        inOrder.verify(assignmentRepository).delete(assignment);
    }

    @Test
    @DisplayName("Should log user deletion action")
    void shouldLogUserDeletionAction() {
        UUID assignmentId = UUID.randomUUID();

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        assignmentService.deleteAssignment(assignmentId);

        verify(authService).getAuthenticatedUser();
    }

    @Test
    @DisplayName("Should handle null assignment gracefully during state validation")
    void shouldHandleNullAssignmentDuringStateValidation() {
        UUID assignmentId = UUID.randomUUID();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.empty());

        AssignmentAlreadyDeletedException exception = assertThrows(
                AssignmentAlreadyDeletedException.class,
                () -> assignmentService.deleteAssignment(assignmentId)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
    }

    @Test
    @DisplayName("Should verify transaction rollback on asset service failure")
    void shouldVerifyTransactionRollbackOnAssetServiceFailure() {
        UUID assignmentId = UUID.randomUUID();

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .user(user)
                .asset(asset)
                .status(waitingForAcceptanceStatus)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));
        doThrow(new RuntimeException("Asset service failure"))
                .when(assetService).updateAssetState(asset, AssetState.AVAILABLE);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> assignmentService.deleteAssignment(assignmentId)
        );

        assertEquals("Asset service failure", exception.getMessage());
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
        verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
        verify(assignmentRepository, never()).delete((Assignment) any());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should validate deletable assignment state using reflection")
    void shouldValidateDeletableAssignmentStateUsingReflection() {
        // Arrange
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .status(waitingForAcceptanceStatus)
                .build();

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                assignmentService,
                "validateAssignmentState",
                assignment,
                AssignmentActionRule.DELETE
        ));
    }

    @Test
    @DisplayName("Should throw AssignmentNotDeletableException for non-deletable state using reflection")
    void shouldThrowAssignmentNotDeletableExceptionForNonDeletableStateUsingReflection() {
        AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                .name("Accepted")
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .status(acceptedStatus)
                .build();

        AssignmentNotDeletableException exception = assertThrows(
                AssignmentNotDeletableException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        assignmentService,
                        "validateAssignmentState",
                        assignment,
                        AssignmentActionRule.DELETE
                )
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_DELETABLE.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("Should load assignment for delete successfully using reflection")
    void shouldLoadAssignmentForDeleteSuccessfullyUsingReflection() {
        UUID assignmentId = UUID.randomUUID();
        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .build();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.of(assignment));

        Assignment result = (Assignment) ReflectionTestUtils.invokeMethod(
                assignmentService,
                "loadAssignmentForDelete",
                assignmentId
        );

        assertEquals(assignment, result);
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
    }

    @Test
    @DisplayName("Should throw AssignmentAlreadyDeletedException when loading assignment for delete fails using reflection")
    void shouldThrowAssignmentAlreadyDeletedExceptionWhenLoadingAssignmentForDeleteFailsUsingReflection() {
        UUID assignmentId = UUID.randomUUID();

        when(assignmentRepository.getAssignmentByIdForDelete(assignmentId))
                .thenReturn(Optional.empty());

        AssignmentAlreadyDeletedException exception = assertThrows(
                AssignmentAlreadyDeletedException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        assignmentService,
                        "loadAssignmentForDelete",
                        assignmentId
                )
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assignmentRepository).getAssignmentByIdForDelete(assignmentId);
    }

    @Test
    @DisplayName("Should release asset in assignment using reflection")
    void shouldReleaseAssetInAssignmentUsingReflection() {
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .build();

        ReflectionTestUtils.invokeMethod(
                assignmentService,
                "releaseAssetInAssignment",
                assignment
        );

        verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
    }

    @Test
    @DisplayName("Should perform delete operations using reflection")
    void shouldPerformDeleteOperationsUsingReflection() {
        UUID assignmentId = UUID.randomUUID();
        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .build();

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        ReflectionTestUtils.invokeMethod(
                assignmentService,
                "performDelete",
                assignment,
                assignmentId
        );

        verify(assignmentRepository).delete(assignment);
        verify(authService).getAuthenticatedUser();
    }

    @Test
    @DisplayName("Should log user deletion action using reflection")
    void shouldLogUserDeletionActionUsingReflection() {
        UUID assignmentId = UUID.randomUUID();

        when(authService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        ReflectionTestUtils.invokeMethod(
                assignmentService,
                "logUserDeletionAction",
                assignmentId
        );

        verify(authService).getAuthenticatedUser();
    }
}