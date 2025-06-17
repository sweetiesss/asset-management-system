package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.request.AssetReturnPageRequest;
import com.nashtech.rookies.oam.dto.request.AssetReturnRequest;
import com.nashtech.rookies.oam.dto.response.AssetReturnPageResponse;
import com.nashtech.rookies.oam.dto.response.AssetReturnResponse;
import com.nashtech.rookies.oam.exception.*;
import com.nashtech.rookies.oam.mapper.AssetReturnMapper;
import com.nashtech.rookies.oam.model.*;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.model.enums.ReturnState;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.repository.AssetReturnRepository;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.AssignmentStatusRepository;
import com.nashtech.rookies.oam.util.SortUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetReturnServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssetReturnRepository assetReturnRepository;

    @Mock
    private AssignmentStatusRepository assignmentStatusRepository;

    @Mock
    private AssetReturnMapper assetReturnMapper;

    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private AssetReturnServiceImpl assetReturnService;

    @Mock
    private Clock clock;

    private AutoCloseable closeable;

    @Mock
    private AssetServiceImpl assetService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    private User createUser(UUID id, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName.getName());

        User user = new User();
        user.setId(id);
        user.setRoles(Set.of(role));
        return user;
    }

    private Assignment createAssignment(User user, String statusName) {
        AssignmentStatus status = new AssignmentStatus();
        status.setName(statusName);

        Assignment assignment = new Assignment();
        assignment.setUser(user);
        assignment.setStatus(status);
        return assignment;
    }

    @Test
    void testCreateAssetReturn_Success() {
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Accepted");

        AssetReturn assetReturn = AssetReturn.builder()
                .assignment(assignment)
                .state(ReturnState.WAITING_FOR_RETURNING)
                .version(0L)
                .build();

        AssetReturnResponse expectedResponse = new AssetReturnResponse();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(assetReturnRepository.findTopByAssignmentOrderByCreatedAtDesc(assignment)).thenReturn(Optional.empty());
        when(assetReturnRepository.save(any())).thenReturn(assetReturn);
        when(assetReturnMapper.toDTO(assetReturn)).thenReturn(expectedResponse);

        AssetReturnResponse result = assetReturnService.createAssetReturn(assignmentId);

        assertEquals(expectedResponse, result);
    }

    @Test
    void testCreateAssetReturn_AssignmentNotFound() {
        UUID assignmentId = UUID.randomUUID();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        AssignmentNotFoundException ex = assertThrows(
                AssignmentNotFoundException.class,
                () -> assetReturnService.createAssetReturn(assignmentId)
        );

        assertEquals(ErrorCode.ASSIGNMENT_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void testCreateAssetReturn_AccessDenied() {
        UUID assignmentId = UUID.randomUUID();
        User currentUser = createUser(UUID.randomUUID(), RoleName.STAFF);
        User otherUser = createUser(UUID.randomUUID(), RoleName.STAFF);
        Assignment assignment = createAssignment(otherUser, "Accepted");

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(currentUser);

        assertThrows(AccessDeniedException.class,
                () -> assetReturnService.createAssetReturn(assignmentId));
    }

    @Test
    void testCreateAssetReturn_AssignmentNotAccepted() {
        UUID assignmentId = UUID.randomUUID();
        User user = createUser(UUID.randomUUID(), RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Waiting for acceptance");

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(user);

        assertThrows(AssignmentNotAcceptedException.class,
                () -> assetReturnService.createAssetReturn(assignmentId));
    }

    @Test
    void testCreateAssetReturn_RequestAlreadyExists() {
        UUID assignmentId = UUID.randomUUID();
        User user = createUser(UUID.randomUUID(), RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Accepted");

        AssetReturn latestReturn = AssetReturn.builder()
                .state(ReturnState.WAITING_FOR_RETURNING)
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(assetReturnRepository.findTopByAssignmentOrderByCreatedAtDesc(assignment))
                .thenReturn(Optional.of(latestReturn));

        assertThrows(RequestReturnAssetAlreadyExistsException.class,
                () -> assetReturnService.createAssetReturn(assignmentId));
    }

    @Test
    void testCreateAssetReturn_AlreadyReturned() {
        UUID assignmentId = UUID.randomUUID();
        User user = createUser(UUID.randomUUID(), RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Accepted");

        AssetReturn latestReturn = AssetReturn.builder()
                .state(ReturnState.COMPLETED)
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(assetReturnRepository.findTopByAssignmentOrderByCreatedAtDesc(assignment))
                .thenReturn(Optional.of(latestReturn));

        assertThrows(AssetAlreadyReturnedException.class,
                () -> assetReturnService.createAssetReturn(assignmentId));
    }

    @Test
    void testUpdateAssetReturn_Success_CompletedState() {
        UUID returnId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Accepted");
        Asset asset = new Asset();
        assignment.setAsset(asset);
        AssetReturn assetReturn = AssetReturn.builder()
                .id(returnId)
                .assignment(assignment)
                .state(ReturnState.WAITING_FOR_RETURNING)
                .version(0L)
                .build();
        AssetReturnRequest request = new AssetReturnRequest();
        request.setState("COMPLETED");
        AssetReturnResponse expectedResponse = new AssetReturnResponse();

        // Mock AssignmentStatus for COMPLETED
        AssignmentStatus completedStatus = new AssignmentStatus();
        completedStatus.setName(AssignmentStatusType.COMPLETED.getDbName());

        LocalDate today = LocalDate.of(2025, 6, 9);
        when(clock.instant()).thenReturn(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(assetReturnRepository.findById(returnId)).thenReturn(Optional.of(assetReturn));
        when(assignmentStatusRepository.findByName(AssignmentStatusType.COMPLETED.getDbName()))
                .thenReturn(Optional.of(completedStatus));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assetReturnRepository.save(any(AssetReturn.class))).thenReturn(assetReturn);
        when(assetReturnMapper.toDTO(assetReturn)).thenReturn(expectedResponse);

        doNothing().when(assetService).updateAssetState(asset, AssetState.AVAILABLE);
        AssetReturnResponse result = assetReturnService.updateAssetReturn(returnId, request);

        assertEquals(expectedResponse, result);
        assertEquals(ReturnState.COMPLETED, assetReturn.getState());
        assertEquals(today, assetReturn.getReturnedDate());
        assertEquals("Completed", assignment.getStatus().getName());
        verify(assignmentRepository).save(assignment);
        verify(assetReturnRepository).save(assetReturn);
        verify(assignmentStatusRepository).findByName(AssignmentStatusType.COMPLETED.getDbName());
        verify(assetService).updateAssetState(asset, AssetState.AVAILABLE);
    }

    @Test
    void testUpdateAssetReturn_Success_CanceledState() {
        UUID returnId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Accepted");
        AssetReturn assetReturn = AssetReturn.builder()
                .id(returnId)
                .assignment(assignment)
                .state(ReturnState.WAITING_FOR_RETURNING)
                .version(0L)
                .build();
        AssetReturnRequest request = new AssetReturnRequest();
        request.setState("CANCELED");
        AssetReturnResponse expectedResponse = new AssetReturnResponse();

        // Mock AssignmentStatus for ACCEPTED
        AssignmentStatus acceptedStatus = new AssignmentStatus();
        acceptedStatus.setName(AssignmentStatusType.ACCEPTED.getDbName());

        when(assetReturnRepository.findById(returnId)).thenReturn(Optional.of(assetReturn));
        when(assignmentStatusRepository.findByName(AssignmentStatusType.ACCEPTED.getDbName()))
                .thenReturn(Optional.of(acceptedStatus));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assetReturnRepository.save(any(AssetReturn.class))).thenReturn(assetReturn);
        when(assetReturnMapper.toDTO(assetReturn)).thenReturn(expectedResponse);

        AssetReturnResponse result = assetReturnService.updateAssetReturn(returnId, request);

        assertEquals(expectedResponse, result);
        assertEquals(ReturnState.CANCELED, assetReturn.getState());
        assertEquals("Accepted", assignment.getStatus().getName());
        verify(assignmentRepository).save(assignment);
        verify(assetReturnRepository).save(assetReturn);
        verify(assignmentStatusRepository).findByName(AssignmentStatusType.ACCEPTED.getDbName());
    }

    @Test
    void testUpdateAssetReturn_AssetReturnNotFound() {
        UUID returnId = UUID.randomUUID();
        AssetReturnRequest request = new AssetReturnRequest();
        request.setState("COMPLETED");

        when(assetReturnRepository.findById(returnId)).thenReturn(Optional.empty());

        RequestReturnNotFoundException ex = assertThrows(
                RequestReturnNotFoundException.class,
                () -> assetReturnService.updateAssetReturn(returnId, request)
        );

        assertEquals(ErrorCode.REQUEST_RETURN_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void testUpdateAssetReturn_InvalidState() {
        UUID returnId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, RoleName.ADMIN);
        Assignment assignment = createAssignment(user, "Accepted");
                AssetReturn assetReturn = AssetReturn.builder()
                        .id(returnId)
                        .assignment(assignment)
                        .state(ReturnState.WAITING_FOR_RETURNING)
                        .version(0L)
                        .build();
        AssetReturnRequest request = new AssetReturnRequest();
        request.setState("INVALID_STATE");

        when(assetReturnRepository.findById(returnId)).thenReturn(Optional.of(assetReturn));

        InvalidRequestReturnStateException ex = assertThrows(
                InvalidRequestReturnStateException.class,
                () -> assetReturnService.updateAssetReturn(returnId, request)
        );

        assertEquals(ErrorCode.INVALID_ASSET_RETURN_STATE.getMessage(), ex.getMessage());
        verify(assignmentRepository, never()).save(any());
        verify(assetReturnRepository, never()).save(any());
    }

    @Test
    void testUpdateAssetReturn_ConcurrentModification() {
        UUID returnId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, RoleName.ADMIN);

        // Create AssignmentStatus for the initial assignment
        AssignmentStatus acceptedStatus = new AssignmentStatus();
        acceptedStatus.setName(AssignmentStatusType.ACCEPTED.getDbName());

        // Create Assignment
        Assignment assignment = createAssignment(user, "Accepted");
        assignment.setStatus(acceptedStatus); // Ensure status is set to avoid null

        // Create AssetReturn
        AssetReturn assetReturn = AssetReturn.builder()
                .id(returnId)
                .assignment(assignment)
                .state(ReturnState.WAITING_FOR_RETURNING)
                .version(0L)
                .build();

        // Create AssetReturnRequest
        AssetReturnRequest request = new AssetReturnRequest();
        request.setState("COMPLETED");

        // Mock AssignmentStatus for COMPLETED
        AssignmentStatus completedStatus = new AssignmentStatus();
        completedStatus.setName(AssignmentStatusType.COMPLETED.getDbName());

        // Mock dependencies
        when(assetReturnRepository.findById(returnId)).thenReturn(Optional.of(assetReturn));
        when(assignmentStatusRepository.findByName(AssignmentStatusType.COMPLETED.getDbName()))
                .thenReturn(Optional.of(completedStatus));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(AssetReturn.class, returnId));
        when(assetReturnRepository.save(any(AssetReturn.class))).thenReturn(assetReturn);

        // Mock clock to avoid null in LocalDate.now(clock)
        LocalDate today = LocalDate.of(2025, 6, 9);
        when(clock.instant()).thenReturn(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ReturnAssetBeingModifiedException ex = assertThrows(
                ReturnAssetBeingModifiedException.class,
                () -> assetReturnService.updateAssetReturn(returnId, request)
        );

        assertEquals(ErrorCode.RETURN_ASSET_BEING_MODIFIED.getMessage(), ex.getMessage());
        verify(assignmentStatusRepository).findByName(AssignmentStatusType.COMPLETED.getDbName());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void testUpdateAssetReturn_AssignmentNotFound() {
        UUID returnId = UUID.randomUUID();
        AssetReturn assetReturn = AssetReturn.builder()
                .id(returnId)
                .assignment(null)
                .state(ReturnState.WAITING_FOR_RETURNING)
                .version(0L)
                .build();
        AssetReturnRequest request = new AssetReturnRequest();
        request.setState("COMPLETED");

        when(assetReturnRepository.findById(returnId)).thenReturn(Optional.of(assetReturn));

        AssignmentNotFoundException ex = assertThrows(
                AssignmentNotFoundException.class,
                () -> assetReturnService.updateAssetReturn(returnId, request)
        );

        assertEquals("Assignment not found for asset return", ex.getMessage());
        verify(assignmentRepository, never()).save(any());
        verify(assetReturnRepository, never()).save(any());
    }

    @Test
    void testGetAssetReturns_WithEmptyStates_UsesDefaultStates() {
        // Arrange
        AssetReturnPageRequest request = new AssetReturnPageRequest();
        request.setPage(0);
        request.setSize(5);
        request.setSort("returnedDate");    // example valid sort field
        request.setSortOrder("ASC");
        request.setStates(Collections.emptyList());  // empty states triggers default
        request.setSearch("searchText");
        request.setReturnedDateFrom(LocalDate.of(2023, 1, 1));
        request.setReturnedDateTo(LocalDate.of(2023, 12, 31));

        User currentUser = new User();
        Location location = new Location();
        location.setId(UUID.randomUUID());
        currentUser.setLocation(location);

        List<String> defaultStates = List.of(ReturnState.WAITING_FOR_RETURNING.name(), ReturnState.COMPLETED.name());

        // Mock auth service to return current user
        when(authService.getAuthenticatedUser()).thenReturn(currentUser);

        // Mock repository returning a page of AssetReturn
        AssetReturn assetReturnEntity = new AssetReturn();
        assetReturnEntity.setState(ReturnState.COMPLETED);

        List<AssetReturn> assetReturns = List.of(assetReturnEntity);
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                SortUtil.buildAssetReturnSort(request.getSort(), request.getSortOrder()));

        Page<AssetReturn> assetReturnPage = new PageImpl<>(assetReturns, pageable, assetReturns.size());

        // We can't easily verify Specification instance passed, so use any()
        when(assetReturnRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(assetReturnPage);

        // Mock mapper
        AssetReturnPageResponse dto = AssetReturnPageResponse.builder()
                .id(UUID.randomUUID())
                .state(ReturnState.COMPLETED)
                .build();

        when(assetReturnMapper.toAssetReturnPageResponse(assetReturnEntity)).thenReturn(dto);

        // Act
        APIPageableResponseDTO<AssetReturnPageResponse> result = assetReturnService.getAssetReturns(request);

        // Assert
        // Confirm default states set in request
        assertEquals(defaultStates, request.getStates());

        // Confirm result page content mapped properly
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(dto, result.getContent().get(0));

        // Verify repository called with a Specification and correct pageable
        verify(assetReturnRepository).findAll(any(Specification.class), eq(pageable));

        // Verify authService called once
        verify(authService).getAuthenticatedUser();
    }

    @Test
    void testGetAssetReturns_WithProvidedStates_UsesGivenStates() {
        // Arrange
        List<String> states = List.of(ReturnState.WAITING_FOR_RETURNING.name());
        AssetReturnPageRequest request = new AssetReturnPageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSort("returnedDate");
        request.setSortOrder("DESC");
        request.setStates(states);
        request.setSearch(null);
        request.setReturnedDateFrom(null);
        request.setReturnedDateTo(null);

        User currentUser = new User();
        Location location = new Location();
        location.setId(UUID.randomUUID());
        currentUser.setLocation(location);

        when(authService.getAuthenticatedUser()).thenReturn(currentUser);

        AssetReturn assetReturnEntity = new AssetReturn();
        assetReturnEntity.setState(ReturnState.WAITING_FOR_RETURNING);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                SortUtil.buildAssetReturnSort(request.getSort(), request.getSortOrder()));

        Page<AssetReturn> assetReturnPage = new PageImpl<>(List.of(assetReturnEntity), pageable, 1);

        when(assetReturnRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(assetReturnPage);

        AssetReturnPageResponse dto = AssetReturnPageResponse.builder()
                .id(UUID.randomUUID())
                .state(ReturnState.WAITING_FOR_RETURNING)
                .build();

        when(assetReturnMapper.toAssetReturnPageResponse(assetReturnEntity)).thenReturn(dto);

        // Act
        APIPageableResponseDTO<AssetReturnPageResponse> result = assetReturnService.getAssetReturns(request);

        // Assert
        assertEquals(states, request.getStates());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(dto, result.getContent().get(0));
        verify(assetReturnRepository).findAll(any(Specification.class), eq(pageable));
        verify(authService).getAuthenticatedUser();
    }
}
