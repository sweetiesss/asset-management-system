package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.request.AssetRequest;
import com.nashtech.rookies.oam.dto.request.UpdateAssetRequest;
import com.nashtech.rookies.oam.dto.response.AssetResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentHistory;
import com.nashtech.rookies.oam.exception.AssetNotDeletableException;
import com.nashtech.rookies.oam.exception.AssetNotEditableException;
import com.nashtech.rookies.oam.exception.AssetNotFoundException;
import com.nashtech.rookies.oam.exception.CategoryNotFoundException;
import com.nashtech.rookies.oam.mapper.AssetMapper;
import com.nashtech.rookies.oam.mapper.AssignmentMapper;
import com.nashtech.rookies.oam.model.*;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.projection.AssignmentWithReturnDate;
import com.nashtech.rookies.oam.projection.EditAssetProjection;
import com.nashtech.rookies.oam.repository.AssetRepository;
import com.nashtech.rookies.oam.repository.AssignmentRepository;
import com.nashtech.rookies.oam.repository.CategoryRepository;
import com.nashtech.rookies.oam.service.AssetCodeGeneratorService;
import com.nashtech.rookies.oam.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetServiceImpl Unit Tests")
class AssetServiceImplTest {

    @Mock
    private AuthService authService;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetMapper assetMapper;

    @Mock
    private AssignmentMapper assignmentMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssetCodeGeneratorService assetCodeGeneratorService;

    @InjectMocks
    private AssetServiceImpl assetService;

    private User mockUser;
    private Location mockLocation;
    private Category mockCategory;
    private Asset mockAsset;
    private AssetRequest mockAssetRequest;
    private UpdateAssetRequest mockUpdateAssetRequest;
    private AssetResponse mockAssetResponse;
    private EditAssetProjection mockEditAssetProjection;
    private AssignmentWithReturnDate mockAssignmentWithReturnDate;
    private AssignmentHistory mockAssignmentHistory;

    @BeforeEach
    void setUp() {
        mockLocation = new Location();
        UUID locationId = UUID.randomUUID();
        mockLocation.setId(locationId);
        mockLocation.setCode("HCM");

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setLocation(mockLocation);

        mockCategory = new Category();
        mockCategory.setId(1);
        mockCategory.setName("Laptop");
        mockCategory.setPrefix("LA");

        mockAsset = new Asset();
        UUID assetId = UUID.randomUUID();
        mockAsset.setId(assetId);
        mockAsset.setCode("LA000001");
        mockAsset.setName("Test Laptop");
        mockAsset.setState(AssetState.AVAILABLE);
        mockAsset.setLocation(mockLocation);
        mockAsset.setCategory(mockCategory);
        mockAsset.setSpecification("Test specification");
        mockAsset.setInstalledDate(LocalDate.now());

        mockAssetRequest = new AssetRequest();
        mockAssetRequest.setName("Test Laptop");
        mockAssetRequest.setCategoryId(1);
        mockAssetRequest.setSpecification("Test specification");
        mockAssetRequest.setInstalledDate(LocalDate.now());
        mockAssetRequest.setState("AVAILABLE");

        mockUpdateAssetRequest = new UpdateAssetRequest();
        mockUpdateAssetRequest.setName("Updated Laptop");
        mockUpdateAssetRequest.setSpecification("Updated specification");
        mockUpdateAssetRequest.setInstalledDate(LocalDate.now().plusDays(1));
        mockUpdateAssetRequest.setState("AVAILABLE");

        mockAssetResponse = new AssetResponse();
        mockAssetResponse.setId(mockAsset.getId());
        mockAssetResponse.setCode("LA000001");
        mockAssetResponse.setName("Test Laptop");

        mockEditAssetProjection = mock(EditAssetProjection.class);
        mockAssignmentWithReturnDate = mock(AssignmentWithReturnDate.class);

        Assignment mockAssignment = new Assignment();
        mockAssignment.setId(UUID.randomUUID());
        mockAssignment.setAsset(mockAsset);
        mockAssignmentHistory = new AssignmentHistory();
    }

    @Test
    @DisplayName("Should create asset successfully")
    void createAsset_ShouldReturnAssetResponse_WhenValidRequest() {
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(mockCategory));
        when(assetCodeGeneratorService.generateAssetCode("LA")).thenReturn("LA000001");
        when(assetMapper.toEntity(mockAssetRequest)).thenReturn(mockAsset);
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        AssetResponse result = assetService.createAsset(mockAssetRequest);

        assertNotNull(result);
        assertEquals(mockAssetResponse.getId(), result.getId());
        assertEquals(mockAssetResponse.getCode(), result.getCode());
        assertEquals(mockAssetResponse.getName(), result.getName());

        verify(authService).getAuthenticatedUser();
        verify(categoryRepository).findById(1);
        verify(assetCodeGeneratorService).generateAssetCode("LA");
        verify(assetMapper).toEntity(mockAssetRequest);
        verify(assetRepository).save(any(Asset.class));
        verify(assetMapper).toResponse(mockAsset);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category not found during creation")
    void createAsset_ShouldThrowCategoryNotFoundException_WhenCategoryNotFound() {
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> assetService.createAsset(mockAssetRequest)
        );

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND.getMessage(), exception.getMessage());
        verify(authService).getAuthenticatedUser();
        verify(categoryRepository).findById(1);
        verify(assetCodeGeneratorService, never()).generateAssetCode(any());
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update asset successfully when asset is editable")
    void updateAsset_ShouldReturnUpdatedAssetResponse_WhenAssetIsIsEditable() {
        UUID assetId = mockAsset.getId();
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        AssetResponse result = assetService.updateAsset(assetId, mockUpdateAssetRequest);

        assertNotNull(result);
        assertEquals(mockAssetResponse.getId(), result.getId());

        verify(authService).getAuthenticatedUser();
        verify(assetRepository).findByIdForUpdate(assetId);
        verify(assetRepository).save(any(Asset.class));
        verify(assetMapper).toResponse(mockAsset);
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when asset not found during update")
    void updateAsset_ShouldThrowAssetNotFoundException_WhenAssetNotFound() {
        UUID assetId = UUID.randomUUID();
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.empty());

        AssetNotFoundException exception = assertThrows(
                AssetNotFoundException.class,
                () -> assetService.updateAsset(assetId, mockUpdateAssetRequest)
        );

        assertEquals(ErrorCode.ASSET_NOT_FOUND.getMessage(), exception.getMessage());
        verify(authService).getAuthenticatedUser();
        verify(assetRepository).findByIdForUpdate(assetId);
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AssetNotEditableException when asset is not in same location")
    void updateAsset_ShouldThrowAssetNotIsEditableException_WhenDifferentLocation() {
        UUID assetId = mockAsset.getId();
        Location differentLocation = new Location();
        differentLocation.setId(UUID.randomUUID());
        differentLocation.setCode("HN");
        mockAsset.setLocation(differentLocation);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));

        AssetNotEditableException exception = assertThrows(
                AssetNotEditableException.class,
                () -> assetService.updateAsset(assetId, mockUpdateAssetRequest)
        );

        assertEquals(ErrorCode.USER_AND_ASSET_LOCATION_MISMATCH.getMessage(), exception.getMessage());
        verify(authService).getAuthenticatedUser();
        verify(assetRepository).findByIdForUpdate(assetId);
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AssetNotEditableException when asset is assigned")
    void updateAsset_ShouldThrowAssetNotIsEditableException_WhenAssetNotAvailable() {
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.ASSIGNED);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));

        AssetNotEditableException exception = assertThrows(
                AssetNotEditableException.class,
                () -> assetService.updateAsset(assetId, mockUpdateAssetRequest)
        );

        assertEquals(ErrorCode.ASSET_NOT_EDITABLE.getMessage(), exception.getMessage());
        verify(assetRepository).findByIdForUpdate(assetId);
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get asset for edit successfully")
    void getAssetForEdit_ShouldReturnAssetResponse_WhenAssetExists() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findProjectedById(assetId)).thenReturn(Optional.of(mockEditAssetProjection));
        when(assetMapper.toResponseEdit(mockEditAssetProjection)).thenReturn(mockAssetResponse);

        AssetResponse result = assetService.getAssetForEdit(assetId);

        assertNotNull(result);
        assertEquals(mockAssetResponse, result);

        verify(assetRepository).findProjectedById(assetId);
        verify(assetMapper).toResponseEdit(mockEditAssetProjection);
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when asset not found for edit")
    void getAssetForEdit_ShouldThrowAssetNotFoundException_WhenAssetNotFound() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findProjectedById(assetId)).thenReturn(Optional.empty());

        AssetNotFoundException exception = assertThrows(
                AssetNotFoundException.class,
                () -> assetService.getAssetForEdit(assetId)
        );

        assertEquals(ErrorCode.ASSET_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assetRepository).findProjectedById(assetId);
        verify(assetMapper, never()).toResponseEdit(any());
    }

    @Test
    @DisplayName("Should verify asset state is updated correctly")
    void updateAsset_ShouldUpdateAssetStateCorrectly() {
        UUID assetId = mockAsset.getId();
        mockUpdateAssetRequest.setState("WAITING_FOR_RECYCLING");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        assetService.updateAsset(assetId, mockUpdateAssetRequest);

        verify(assetRepository).save(argThat(asset -> asset.getState() == AssetState.WAITING_FOR_RECYCLING &&
                asset.getName().equals(mockUpdateAssetRequest.getName()) &&
                asset.getSpecification().equals(mockUpdateAssetRequest.getSpecification()) &&
                asset.getInstalledDate().equals(mockUpdateAssetRequest.getInstalledDate())));
    }

    @Test
    @DisplayName("Should verify asset properties are set correctly during creation")
    void createAsset_ShouldSetAssetPropertiesCorrectly() {
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(mockCategory));
        when(assetCodeGeneratorService.generateAssetCode("LA")).thenReturn("LA000001");
        when(assetMapper.toEntity(mockAssetRequest)).thenReturn(mockAsset);
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        assetService.createAsset(mockAssetRequest);

        verify(assetRepository).save(argThat(asset -> asset.getLocation().equals(mockLocation) &&
                asset.getCategory().equals(mockCategory) &&
                asset.getCode().equals("LA000001")));
    }

    @Test
    @DisplayName("Should get asset detail successfully")
    void getAssetDetail_ShouldReturnAssetResponse_WhenAssetExists() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        AssetResponse result = assetService.getAssetDetail(assetId);

        assertNotNull(result);
        assertEquals(mockAssetResponse, result);

        verify(assetRepository).findById(assetId);
        verify(assetMapper).toResponse(mockAsset);
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when asset not found for detail")
    void getAssetDetail_ShouldThrowAssetNotFoundException_WhenAssetNotFound() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

        AssetNotFoundException exception = assertThrows(
                AssetNotFoundException.class,
                () -> assetService.getAssetDetail(assetId)
        );

        assertEquals(ErrorCode.ASSET_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assetRepository).findById(assetId);
        verify(assetMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should get assignment history successfully")
    void getAssignmentHistory_ShouldReturnPagedResponse_WhenAssignmentsExist() {
        UUID assetId = UUID.randomUUID();
        Integer pageNo = 0;
        Integer pageSize = 10;
        PageRequest expectedPageRequest = PageRequest.of(pageNo, pageSize);
        List<String> defaultStatuses = List.of(
                AssignmentStatusType.ACCEPTED.getDbName(),
                AssignmentStatusType.COMPLETED.getDbName()
        );
        List<AssignmentWithReturnDate> assignments = List.of(mockAssignmentWithReturnDate);
        Page<AssignmentWithReturnDate> assignmentPage = new PageImpl<>(assignments, expectedPageRequest, 1);
        Page<AssignmentHistory> historyPage = new PageImpl<>(List.of(mockAssignmentHistory), expectedPageRequest, 1);

        when(assignmentRepository.findAllWithReturnDateByAsset_Id(assetId, defaultStatuses, expectedPageRequest))
                .thenReturn(assignmentPage);
        when(assignmentMapper.toAssignmentHistory(mockAssignmentWithReturnDate))
                .thenReturn(mockAssignmentHistory);

        var result = assetService.getAssignmentHistory(assetId, pageNo, pageSize);

        assertNotNull(result);
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(1, result.getPageable().getTotalElements());

        verify(assignmentRepository).findAllWithReturnDateByAsset_Id(assetId, defaultStatuses, expectedPageRequest);
        verify(assignmentMapper).toAssignmentHistory(mockAssignmentWithReturnDate);
    }

    @Test
    @DisplayName("Should get assignment history with empty result")
    void getAssignmentHistory_ShouldReturnEmptyPage_WhenNoAssignmentsExist() {
        UUID assetId = UUID.randomUUID();
        Integer pageNo = 0;
        Integer pageSize = 10;
        PageRequest expectedPageRequest = PageRequest.of(pageNo, pageSize);

        Page<AssignmentWithReturnDate> emptyAssignmentPage = new PageImpl<>(List.of(), expectedPageRequest, 0);
        Page<AssignmentHistory> emptyHistoryPage = new PageImpl<>(List.of(), expectedPageRequest, 0);
        List<String> defaultStatuses = List.of(
                AssignmentStatusType.ACCEPTED.getDbName(),
                AssignmentStatusType.COMPLETED.getDbName()
        );
        when(assignmentRepository.findAllWithReturnDateByAsset_Id(assetId, defaultStatuses, expectedPageRequest))
                .thenReturn(emptyAssignmentPage);

        var result = assetService.getAssignmentHistory(assetId, pageNo, pageSize);

        assertNotNull(result);
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getPageable().getTotalElements());

        verify(assignmentRepository).findAllWithReturnDateByAsset_Id(assetId, defaultStatuses, expectedPageRequest);
        verify(assignmentMapper, never()).toAssignmentHistory(any());
    }
    @Test
    @DisplayName("Should throw exception when updating asset with invalid state")
    void updateAsset_ShouldThrowException_WhenStateIsInvalid() {
        // Given
        UUID assetId = mockAsset.getId();
        mockUpdateAssetRequest.setState("INVALID_STATE");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                assetService.updateAsset(assetId, mockUpdateAssetRequest)
        );

        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should map correct category and location during asset creation")
    void createAsset_ShouldMapCategoryAndLocationProperly() {
        // Given
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(mockCategory));
        when(assetCodeGeneratorService.generateAssetCode("LA")).thenReturn("LA000001");
        when(assetMapper.toEntity(mockAssetRequest)).thenReturn(mockAsset);
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        // When
        assetService.createAsset(mockAssetRequest);

        // Then
        verify(assetRepository).save(argThat(asset ->
                asset.getCategory().getId().equals(mockCategory.getId()) &&
                        asset.getLocation().getId().equals(mockLocation.getId())
        ));
    }

    @Test
    @DisplayName("Should set updated fields properly during asset update")
    void updateAsset_ShouldSetUpdatedFieldsProperly() {
        // Given
        UUID assetId = mockAsset.getId();
        mockUpdateAssetRequest.setName("Updated Name");
        mockUpdateAssetRequest.setSpecification("Updated Spec");
        mockUpdateAssetRequest.setInstalledDate(LocalDate.now().plusDays(3));
        mockUpdateAssetRequest.setState("AVAILABLE");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        // When
        assetService.updateAsset(assetId, mockUpdateAssetRequest);

        // Then
        verify(assetRepository).save(argThat(asset ->
                asset.getName().equals("Updated Name") &&
                        asset.getSpecification().equals("Updated Spec") &&
                        asset.getInstalledDate().equals(mockUpdateAssetRequest.getInstalledDate()) &&
                        asset.getState() == AssetState.AVAILABLE
        ));
    }


    @Test
    @DisplayName("Should handle null page parameters in assignment history")
    void getAssignmentHistory_ShouldHandleNullPageParameters() {
        UUID assetId = UUID.randomUUID();

        Page<AssignmentWithReturnDate> assignmentPage = new PageImpl<>(List.of());

        when(assignmentRepository.findAllWithReturnDateByAsset_Id(eq(assetId), any(), any(PageRequest.class)))
                .thenReturn(assignmentPage);

        var result = assetService.getAssignmentHistory(assetId, null, null);

        assertNotNull(result);
        verify(assignmentRepository).findAllWithReturnDateByAsset_Id(eq(assetId), any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should verify editable method returns false for different location")
    void isEditable_ShouldReturnFalse_WhenDifferentLocation() {
        Location differentLocation = new Location();
        differentLocation.setId(UUID.randomUUID());
        differentLocation.setCode("DN");

        Asset assetInDifferentLocation = new Asset();
        assetInDifferentLocation.setId(UUID.randomUUID());
        assetInDifferentLocation.setLocation(differentLocation);
        assetInDifferentLocation.setState(AssetState.AVAILABLE);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(any(UUID.class))).thenReturn(Optional.of(assetInDifferentLocation));

        assertThrows(AssetNotEditableException.class,
                () -> assetService.updateAsset(assetInDifferentLocation.getId(), mockUpdateAssetRequest));
    }

    @Test
    @DisplayName("Should verify editable method returns false for assigned asset")
    void isEditable_ShouldReturnFalse_WhenAssetIsAssigned() {
        Asset assignedAsset = new Asset();
        assignedAsset.setId(UUID.randomUUID());
        assignedAsset.setLocation(mockLocation);
        assignedAsset.setState(AssetState.ASSIGNED);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(any(UUID.class))).thenReturn(Optional.of(assignedAsset));

        assertThrows(AssetNotEditableException.class,
                () -> assetService.updateAsset(assignedAsset.getId(), mockUpdateAssetRequest));
    }

    @Test
    @DisplayName("Should verify editable method returns true for same location and available asset")
    void isEditable_ShouldReturnTrue_WhenSameLocationAndAvailable() {
        UUID assetId = mockAsset.getId();

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        AssetResponse result = assetService.updateAsset(assetId, mockUpdateAssetRequest);

        assertNotNull(result);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("Should handle different asset states during update")
    void updateAsset_ShouldHandleDifferentAssetStates() {
        UUID assetId = mockAsset.getId();

        mockUpdateAssetRequest.setState("WAITING_FOR_RECYCLING");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        assetService.updateAsset(assetId, mockUpdateAssetRequest);

        verify(assetRepository).save(argThat(asset ->
                asset.getState() == AssetState.WAITING_FOR_RECYCLING
        ));
    }

    @Test
    @DisplayName("Should handle case insensitive state conversion")
    void updateAsset_ShouldHandleCaseInsensitiveState() {
        UUID assetId = mockAsset.getId();

        mockUpdateAssetRequest.setState("available");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        assetService.updateAsset(assetId, mockUpdateAssetRequest);

        verify(assetRepository).save(argThat(asset ->
                asset.getState() == AssetState.AVAILABLE
        ));
    }

    @Test
    @DisplayName("Should verify all asset fields are updated correctly")
    void updateAsset_ShouldUpdateAllFields() {
        UUID assetId = mockAsset.getId();
        LocalDate newDate = LocalDate.now().plusDays(5);

        mockUpdateAssetRequest.setName("New Asset Name");
        mockUpdateAssetRequest.setSpecification("New Specification");
        mockUpdateAssetRequest.setInstalledDate(newDate);
        mockUpdateAssetRequest.setState("NOT_AVAILABLE");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        assetService.updateAsset(assetId, mockUpdateAssetRequest);

        verify(assetRepository).save(argThat(asset ->
                asset.getName().equals("New Asset Name") &&
                        asset.getSpecification().equals("New Specification") &&
                        asset.getInstalledDate().equals(newDate) &&
                        asset.getState() == AssetState.NOT_AVAILABLE
        ));
    }

    @Test
    @DisplayName("Should delete asset successfully when asset is deletable")
    void deleteAsset_ShouldDeleteAsset_WhenAssetIsDeletable() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.AVAILABLE); // Not assigned

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assignmentRepository.existsByAsset(mockAsset)).thenReturn(false); // Never assigned

        // When
        assetService.deleteAsset(assetId);

        // Then
        verify(authService).getAuthenticatedUser();
        verify(assetRepository).findById(assetId);
        verify(assignmentRepository).existsByAsset(mockAsset);
        verify(assetRepository).delete(mockAsset);
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when asset not found for deletion")
    void deleteAsset_ShouldThrowAssetNotFoundException_WhenAssetNotFound() {
        // Given
        UUID assetId = UUID.randomUUID();

        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

        // When & Then
        AssetNotFoundException exception = assertThrows(
                AssetNotFoundException.class,
                () -> assetService.deleteAsset(assetId)
        );

        assertEquals(ErrorCode.ASSET_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).delete((Asset) any());
    }

    @Test
    @DisplayName("Should throw AssetNotDeletableException when asset is in different location")
    void deleteAsset_ShouldThrowAssetNotDeletableException_WhenDifferentLocation() {
        // Given
        UUID assetId = mockAsset.getId();
        Location differentLocation = new Location();
        differentLocation.setId(UUID.randomUUID());
        differentLocation.setCode("HN");
        mockAsset.setLocation(differentLocation);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));

        // When & Then
        AssetNotDeletableException exception = assertThrows(
                AssetNotDeletableException.class,
                () -> assetService.deleteAsset(assetId)
        );

        assertEquals(ErrorCode.USER_AND_ASSET_LOCATION_MISMATCH.getMessage(), exception.getMessage());
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).delete((Asset) any());
    }

    @Test
    @DisplayName("Should throw AssetNotDeletableException when asset was previously assigned")
    void deleteAsset_ShouldThrowAssetNotDeletableException_WhenAssetWasPreviouslyAssigned() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.AVAILABLE); // Currently available but was assigned before

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assignmentRepository.existsByAsset(mockAsset)).thenReturn(true); // Was assigned before

        // When & Then
        AssetNotDeletableException exception = assertThrows(
                AssetNotDeletableException.class,
                () -> assetService.deleteAsset(assetId)
        );

        assertEquals(ErrorCode.ASSET_NOT_DELETABLE.getMessage(), exception.getMessage());
        verify(assignmentRepository).existsByAsset(mockAsset);
        verify(assetRepository, never()).delete((Asset) any());
    }

    @Test
    @DisplayName("Should verify all conditions for deletable asset")
    void deleteAsset_ShouldVerifyAllDeletableConditions() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.NOT_AVAILABLE); // Unassigned state
        mockAsset.setLocation(mockLocation); // Same location as user

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assignmentRepository.existsByAsset(mockAsset)).thenReturn(false); // Never assigned

        // When
        assetService.deleteAsset(assetId);

        // Then
        verify(authService).getAuthenticatedUser();
        verify(assetRepository).findById(assetId);
        verify(assignmentRepository).existsByAsset(mockAsset);
        verify(assetRepository).delete(mockAsset);
    }

    @Test
    @DisplayName("Should verify asset deletion with WAITING_FOR_RECYCLING state")
    void deleteAsset_ShouldDeleteAsset_WhenAssetIsWaitingForRecycling() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.WAITING_FOR_RECYCLING); // Unassigned state

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assignmentRepository.existsByAsset(mockAsset)).thenReturn(false);

        // When
        assetService.deleteAsset(assetId);

        // Then
        verify(assetRepository).delete(mockAsset);
    }

    @Test
    @DisplayName("Should verify asset deletion with RECYCLED state")
    void deleteAsset_ShouldDeleteAsset_WhenAssetIsRecycled() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.RECYCLED); // Unassigned state

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assignmentRepository.existsByAsset(mockAsset)).thenReturn(false);

        // When
        assetService.deleteAsset(assetId);

        // Then
        verify(assetRepository).delete(mockAsset);
    }

    @Test
    @DisplayName("Should check if asset is available")
    void isAssetAvailable_ShouldReturnTrue_WhenAssetStateIsAvailable() {
        // Given
        mockAsset.setState(AssetState.AVAILABLE);

        // When
        boolean result = assetService.isAssetAvailable(mockAsset);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should check if asset is not available")
    void isAssetAvailable_ShouldReturnFalse_WhenAssetStateIsNotAvailable() {
        // Given
        mockAsset.setState(AssetState.ASSIGNED);

        // When
        boolean result = assetService.isAssetAvailable(mockAsset);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should update asset state successfully")
    void updateAssetState_ShouldUpdateStateAndSaveAsset() {
        // Given
        AssetState newState = AssetState.ASSIGNED;
        when(assetRepository.save(mockAsset)).thenReturn(mockAsset);

        // When
        assetService.updateAssetState(mockAsset, newState);

        // Then
        assertEquals(AssetState.ASSIGNED, mockAsset.getState());
        verify(assetRepository).save(mockAsset);
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when optimistic locking failure occurs during deletion")
    void deleteAsset_ShouldThrowAssetNotFoundException_WhenOptimisticLockingFailureOnDelete() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.AVAILABLE);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(mockAsset));
        when(assignmentRepository.existsByAsset(mockAsset)).thenReturn(false);
        doThrow(new OptimisticLockingFailureException("Optimistic locking failure"))
                .when(assetRepository).delete(mockAsset);

        // When & Then
        AssetNotFoundException exception = assertThrows(
                AssetNotFoundException.class,
                () -> assetService.deleteAsset(assetId)
        );

        assertEquals(ErrorCode.ASSET_NOT_FOUND.getMessage(), exception.getMessage());
        verify(assetRepository).delete(mockAsset);
    }

    @Test
    @DisplayName("Should validate asset is not assigned correctly - should throw when asset is assigned")
    void validateAssetNotAssigned_ShouldThrowException_WhenAssetIsNotAssigned() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.ASSIGNED);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));

        // When & Then
        AssetNotEditableException exception = assertThrows(
                AssetNotEditableException.class,
                () -> assetService.updateAsset(assetId, mockUpdateAssetRequest)
        );

        assertEquals(ErrorCode.ASSET_NOT_EDITABLE.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully update asset when asset is not assigned (editable state)")
    void updateAsset_ShouldSucceed_WhenAssetIsAssigned() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.AVAILABLE);

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        // When
        AssetResponse result = assetService.updateAsset(assetId, mockUpdateAssetRequest);

        // Then
        assertNotNull(result);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("Should handle all valid asset states in enum conversion")
    void applyUpdateToAsset_ShouldHandleAllValidAssetStates() {
        // Test all valid enum values
        String[] validStates = {"AVAILABLE", "NOT_AVAILABLE", "ASSIGNED", "WAITING_FOR_RECYCLING", "RECYCLED"};

        for (String stateString : validStates) {
            // Given
            UUID assetId = mockAsset.getId();
            mockAsset.setState(AssetState.AVAILABLE); // Set to editable state
            mockUpdateAssetRequest.setState(stateString);

            when(authService.getAuthenticatedUser()).thenReturn(mockUser);
            when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
            when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
            when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

            // When
            assetService.updateAsset(assetId, mockUpdateAssetRequest);

            // Then
            AssetState expectedState = AssetState.valueOf(stateString);
            verify(assetRepository).save(argThat(asset -> asset.getState() == expectedState));

            // Reset for next iteration
            reset(assetRepository);
        }
    }

    @Test
    @DisplayName("Should apply all update fields correctly in applyUpdateToAsset")
    void applyUpdateToAsset_ShouldApplyAllFieldsCorrectly() {
        // Given
        UUID assetId = mockAsset.getId();
        mockAsset.setState(AssetState.AVAILABLE);

        LocalDate newInstalledDate = LocalDate.now().plusDays(10);
        mockUpdateAssetRequest.setName("Complete New Name");
        mockUpdateAssetRequest.setSpecification("Complete New Specification");
        mockUpdateAssetRequest.setInstalledDate(newInstalledDate);
        mockUpdateAssetRequest.setState("WAITING_FOR_RECYCLING");

        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(assetRepository.findByIdForUpdate(assetId)).thenReturn(Optional.of(mockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(mockAsset);
        when(assetMapper.toResponse(mockAsset)).thenReturn(mockAssetResponse);

        // When
        assetService.updateAsset(assetId, mockUpdateAssetRequest);

        // Then
        verify(assetRepository).save(argThat(asset ->
                asset.getName().equals("Complete New Name") &&
                        asset.getSpecification().equals("Complete New Specification") &&
                        asset.getInstalledDate().equals(newInstalledDate) &&
                        asset.getState() == AssetState.WAITING_FOR_RECYCLING
        ));
    }

}