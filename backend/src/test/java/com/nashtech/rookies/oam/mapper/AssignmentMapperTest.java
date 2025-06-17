package com.nashtech.rookies.oam.mapper;

import com.nashtech.rookies.oam.dto.request.AssignmentRequest;
import com.nashtech.rookies.oam.dto.response.AssignmentDetailResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentHistory;
import com.nashtech.rookies.oam.dto.response.AssignmentPageResponse;
import com.nashtech.rookies.oam.dto.response.AssignmentResponse;
import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.projection.AssignmentWithReturnDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentMapperTest {

    private AssignmentMapper assignmentMapper;

    @BeforeEach
    void setUp() {
        assignmentMapper = new AssignmentMapperImpl();
    }

    @Test
    void toEntity_ShouldMapAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        AssignmentRequest request = new AssignmentRequest();
        request.setUserId(userId);
        request.setAssetId(assetId);
        request.setAssignedDate(LocalDate.now());
        request.setNote("Test note");

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setCode("AST001");
        asset.setName("Laptop");
        asset.setState(AssetState.AVAILABLE);

        AssignmentStatus status = new AssignmentStatus();
        status.setId(1);
        status.setName("Waiting for acceptance");

        Assignment assignment = assignmentMapper.toEntity(request, user, asset, status);

        assertThat(assignment).isNotNull();
        assertThat(assignment.getId()).isNull(); // Should be ignored
        assertThat(assignment.getNote()).isEqualTo(request.getNote());
        assertThat(assignment.getUser()).isEqualTo(user);
        assertThat(assignment.getAsset()).isEqualTo(asset);
        assertThat(assignment.getAssignedDate()).isEqualTo(request.getAssignedDate());
    }

    @Test
    void toEntity_ShouldHandleNullNote() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        AssignmentRequest request = new AssignmentRequest();
        request.setUserId(userId);
        request.setAssetId(assetId);
        request.setAssignedDate(LocalDate.now());
        request.setNote(null);

        User user = new User();
        Asset asset = new Asset();
        AssignmentStatus status = new AssignmentStatus();

        Assignment assignment = assignmentMapper.toEntity(request, user, asset, status);

        assertThat(assignment).isNotNull();
        assertThat(assignment.getNote()).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFieldsCorrectly() {
        UUID assignmentId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setCode("AST002");
        asset.setName("Monitor");

        AssignmentStatus status = new AssignmentStatus();
        status.setId(2);
        status.setName("Accepted");

        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setUser(user);
        assignment.setAsset(asset);
        assignment.setStatus(status);
        assignment.setAssignedDate(LocalDate.of(2025, 5, 28));

        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(assignmentId);
        assertThat(response.getAssetCode()).isEqualTo(asset.getCode());
        assertThat(response.getAssetName()).isEqualTo(asset.getName());
        assertThat(response.getAssignTo()).isEqualTo(user.getUsername());
        assertThat(response.getAssignedDate()).isEqualTo(assignment.getAssignedDate());
        assertThat(response.getState()).isEqualTo(status);
    }

    @Test
    void toResponse_ShouldHandleNulls() {
        Assignment assignment = new Assignment();

        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getAssetCode()).isNull();
        assertThat(response.getAssetName()).isNull();
        assertThat(response.getAssignTo()).isNull();
        assertThat(response.getAssignBy()).isNull();
        assertThat(response.getAssignedDate()).isNull();
        assertThat(response.getState()).isNull();
    }

    @Test
    void toAssignmentHistory_ShouldMapAllFieldsCorrectly() {
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");

        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setCode("AST001");

        AssignmentStatus status = new AssignmentStatus();
        status.setId(1);
        status.setName("Accepted");

        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setUser(user);
        assignment.setAsset(asset);
        assignment.setStatus(status);
        assignment.setAssignedDate(LocalDate.of(2025, 5, 28));


        AssignmentWithReturnDate projection = mock(AssignmentWithReturnDate.class);
        when(projection.getAssignment()).thenReturn(assignment);
        when(projection.getReturnedDate()).thenReturn(LocalDate.of(2025, 6, 15));

        AssignmentHistory history = assignmentMapper.toAssignmentHistory(projection);

        assertThat(history).isNotNull();
        assertThat(history.getAssignedDate()).isEqualTo(LocalDate.of(2025, 5, 28));
        assertThat(history.getAssignedTo()).isEqualTo("John Doe");
        assertThat(history.getReturnedDate()).isEqualTo(LocalDate.of(2025, 6, 15));
    }

    @Test
    void toAssignmentHistory_ShouldHandleNulls() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName(null);

        Assignment assignment = new Assignment();
        assignment.setUser(user);
        assignment.setAssignedDate(null);

        AssignmentWithReturnDate projection = mock(AssignmentWithReturnDate.class);
        when(projection.getAssignment()).thenReturn(assignment);
        when(projection.getReturnedDate()).thenReturn(null);

        AssignmentHistory history = assignmentMapper.toAssignmentHistory(projection);

        assertThat(history).isNotNull();
        assertThat(history.getAssignedDate()).isNull();
        assertThat(history.getAssignedTo()).isEqualTo("Jane null");
        assertThat(history.getAssignedBy()).isNull();
        assertThat(history.getReturnedDate()).isNull();
    }

    @Test
    void toAssignmentPageResponse_ShouldMapAllFieldsCorrectly() {
        UUID assignmentId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setCode("AST100");
        asset.setName("Keyboard");

        AssignmentStatus status = new AssignmentStatus();
        status.setId(3);
        status.setName("Declined");

        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setUser(user);
        assignment.setAsset(asset);
        assignment.setStatus(status);
        assignment.setAssignedDate(LocalDate.of(2025, 6, 1));

        AssignmentPageResponse response = assignmentMapper.toAssignmentPageResponse(assignment);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(assignmentId);
        assertThat(response.getAssetCode()).isEqualTo("AST100");
        assertThat(response.getAssetName()).isEqualTo("Keyboard");
        assertThat(response.getUserId()).isEqualTo("testuser");
        assertThat(response.getAssignedDate()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(response.getStatus()).isEqualTo(status);
    }

    @Test
    void toAssignmentPageResponse_ShouldHandleNullsGracefully() {
        Assignment assignment = new Assignment();

        AssignmentPageResponse response = assignmentMapper.toAssignmentPageResponse(assignment);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getAssetCode()).isNull();
        assertThat(response.getAssetName()).isNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getCreatedBy()).isNull();
        assertThat(response.getAssignedDate()).isNull();
        assertThat(response.getStatus()).isNull();
    }

    @Test
    @DisplayName("Should map Assignment entity to AssignmentDetailResponse correctly")
    void shouldMapAssignmentToAssignmentDetailResponse() {
        // Arrange
        Asset asset = Asset.builder()
                .code("ASSET123")
                .name("Laptop")
                .specification("16GB RAM, 512GB SSD")
                .build();

        User user = User.builder()
                .username("johndoe")
                .build();

        AssignmentStatus status = AssignmentStatus.builder()
                .id(1)
                .name("Accepted")
                .build();

        Assignment assignment = Assignment.builder()
                .asset(asset)
                .user(user)
                .status(status)
                .assignedDate(LocalDate.of(2024, 5, 10))
                .note("Test note")
                .build();

        // Act
        AssignmentDetailResponse result = assignmentMapper.toDetailResponse(assignment);

        // Assert
        assertThat(result.getAssetCode()).isEqualTo("ASSET123");
        assertThat(result.getAssetName()).isEqualTo("Laptop");
        assertThat(result.getSpecification()).isEqualTo("16GB RAM, 512GB SSD");
        assertThat(result.getUserId()).isEqualTo("johndoe");
        assertThat(result.getAssignedDate()).isEqualTo(LocalDate.of(2024, 5, 10));
        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(result.getNote()).isEqualTo("Test note");
    }
}