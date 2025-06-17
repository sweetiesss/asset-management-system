package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.model.*;
import com.nashtech.rookies.oam.model.enums.AssetState;
import com.nashtech.rookies.oam.model.enums.AssignmentStatusType;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.repository.*;
import com.nashtech.rookies.oam.util.ExcelBuilder;
import com.nashtech.rookies.oam.util.SortUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl Unit Tests")
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepo;

    @Mock
    private AssetRepository assetRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AssignmentRepository assignmentRepo;

    @Mock
    private AssignmentStatusRepository assignmentStatusRepo;

    @Mock
    private ExcelBuilder excelBuilder;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Report mockReport;
    private Asset mockAsset;
    private User mockUser;
    private Assignment mockAssignment;
    private Sort mockSort;
    private byte[] mockExcelData;

    @BeforeEach
    void setUp() {
        // Mock Report
        mockReport = new Report();
        mockReport.setId(1);
        mockReport.setCategory("Laptops");
        mockReport.setTotal(100);
        mockReport.setAssigned(50);
        mockReport.setAvailable(30);
        mockReport.setNotAvailable(10);
        mockReport.setWaitingForRecycling(5);
        mockReport.setRecycled(5);

        // Mock Asset
        Category category = new Category();
        category.setId(1);
        category.setName("Laptops");

        Location location = new Location();
        location.setId(UUID.randomUUID());
        location.setName("HN");

        mockAsset = Asset.builder()
                .id(UUID.randomUUID())
                .code("LA000001")
                .name("Dell Laptop")
                .specification("Dell Inspiron 15")
                .installedDate(LocalDate.now())
                .state(AssetState.AVAILABLE)
                .category(category)
                .location(location)
                .build();

        // Mock User
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(RoleName.STAFF.getName());

        mockUser = User.builder()
                .id(UUID.randomUUID())
                .staffCode("SD0001")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .joinedOn(LocalDate.now())
                .gender(Gender.MALE)
                .roles(Set.of(role))
                .location(location)
                .build();

        // Mock Assignment
        mockAssignment = Assignment.builder()
                .id(UUID.randomUUID())
                .asset(mockAsset)
                .user(mockUser)
                .assignedDate(LocalDate.now())
                .note("Test assignment")
                .status(AssignmentStatus.builder()
                        .id(1)
                        .name(AssignmentStatusType.ACCEPTED.getDbName())
                        .build())
                .build();

        mockSort = Sort.by(Sort.Direction.ASC, "category");
        mockExcelData = "mock excel data".getBytes();
    }

    // ========== GET REPORT TESTS ==========

    @Test
    @DisplayName("Should get report with valid parameters")
    void shouldGetReportWithValidParameters() {
        // Given
        int pageNo = 0;
        int pageSize = 20;
        String sortField = "category";
        String sortOrder = "asc";

        Page<Report> mockPage = new PageImpl<>(List.of(mockReport), PageRequest.of(pageNo, pageSize, mockSort), 1);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(reportRepo.findAll(any(PageRequest.class))).thenReturn(mockPage);

            // When
            APIPageableResponseDTO<Report> result = reportService.getReport(pageNo, pageSize, sortField, sortOrder);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Laptops", result.getContent().get(0).getCategory());
            assertEquals(100, result.getContent().get(0).getTotal());

            sortUtilMock.verify(() -> SortUtil.buildAssetReportSort(sortField, sortOrder));
            verify(reportRepo).findAll(any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Should return empty result when no reports found")
    void shouldReturnEmptyResultWhenNoReportsFound() {
        // Given
        int pageNo = 0;
        int pageSize = 20;
        String sortField = "category";
        String sortOrder = "asc";

        Page<Report> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(pageNo, pageSize, mockSort), 0);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(reportRepo.findAll(any(PageRequest.class))).thenReturn(emptyPage);

            // When
            APIPageableResponseDTO<Report> result = reportService.getReport(pageNo, pageSize, sortField, sortOrder);

            // Then
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getPageable().getTotalElements());
        }
    }

    // ========== STANDARD EXPORT TESTS ==========

    @Test
    @DisplayName("Should export standard report successfully")
    void shouldExportStandardReportSuccessfully() throws IOException {
        // Given
        String sortField = "category";
        String sortOrder = "asc";
        List<Report> reportList = List.of(mockReport);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(reportRepo.findAll(mockSort)).thenReturn(reportList);
            when(excelBuilder.build(eq(reportList), eq("Report_Export"), eq("Standard Asset Report"), 
                    eq(Optional.empty()), eq(Optional.empty()), eq(Set.of("id")))).thenReturn(mockExcelData);

            // When
            byte[] result = reportService.standardExport(sortField, sortOrder);

            // Then
            assertNotNull(result);
            assertArrayEquals(mockExcelData, result);

            sortUtilMock.verify(() -> SortUtil.buildAssetReportSort(sortField, sortOrder));
            verify(reportRepo).findAll(mockSort);
            verify(excelBuilder).build(eq(reportList), eq("Report_Export"), eq("Standard Asset Report"), 
                    eq(Optional.empty()), eq(Optional.empty()), eq(Set.of("id")));
        }
    }

    @Test
    @DisplayName("Should handle IOException in standard export")
    void shouldHandleIOExceptionInStandardExport() throws IOException {
        // Given
        String sortField = "category";
        String sortOrder = "asc";
        List<Report> reportList = List.of(mockReport);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(reportRepo.findAll(mockSort)).thenReturn(reportList);
            when(excelBuilder.build(any(), anyString(), anyString(), any(), any(), any()))
                    .thenThrow(new IOException("Excel generation failed"));

            // When & Then
            assertThrows(IOException.class, () -> reportService.standardExport(sortField, sortOrder));

            verify(reportRepo).findAll(mockSort);
            verify(excelBuilder).build(any(), anyString(), anyString(), any(), any(), any());
        }
    }

    // ========== DYNAMIC ASSET REPORT TESTS ==========

    @Test
    @DisplayName("Should generate dynamic asset report successfully")
    void shouldGenerateDynamicAssetReportSuccessfully() throws IOException {
        // Given
        String sortField = "code";
        String sortOrder = "asc";
        String fileName = "asset_report";
        List<Integer> categoryIds = List.of(1, 2);
        List<String> states = List.of("AVAILABLE", "ASSIGNED");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<Asset> assetList = List.of(mockAsset);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(assetRepo.findAll(ArgumentMatchers.<Specification<Asset>>any(), eq(mockSort))).thenReturn(assetList);
            when(excelBuilder.build(any(), eq(fileName), eq("Asset Report"), 
                    eq(Optional.of(startDate)), eq(Optional.of(endDate)))).thenReturn(mockExcelData);

            // When
            byte[] result = reportService.generateDynamicAssetReport(sortField, sortOrder, fileName, 
                    categoryIds, states, startDate, endDate);

            // Then
            assertNotNull(result);
            assertArrayEquals(mockExcelData, result);

            verify(assetRepo).findAll(ArgumentMatchers.<Specification<Asset>>any(), eq(mockSort));
            verify(excelBuilder).build(any(), eq(fileName), eq("Asset Report"), 
                    eq(Optional.of(startDate)), eq(Optional.of(endDate)));
        }
    }

    // ========== DYNAMIC USER REPORT TESTS ==========

    @Test
    @DisplayName("Should generate dynamic user report successfully")
    void shouldGenerateDynamicUserReportSuccessfully() throws IOException {
        // Given
        String sortField = "username";
        String sortOrder = "asc";
        String fileName = "user_report";
        List<String> roleTypes = List.of("ADMIN", "STAFF");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<User> userList = List.of(mockUser);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildUserReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(userRepo.findAll(ArgumentMatchers.<Specification<User>>any(), eq(mockSort))).thenReturn(userList);
            when(excelBuilder.build(any(), eq(fileName), eq("User Report"), 
                    eq(Optional.of(startDate)), eq(Optional.of(endDate)))).thenReturn(mockExcelData);

            // When
            byte[] result = reportService.generateDynamicUserReport(sortField, sortOrder, fileName, 
                    roleTypes, startDate, endDate);

            // Then
            assertNotNull(result);
            assertArrayEquals(mockExcelData, result);

            verify(userRepo).findAll(ArgumentMatchers.<Specification<User>>any(), eq(mockSort));
            verify(excelBuilder).build(any(), eq(fileName), eq("User Report"), 
                    eq(Optional.of(startDate)), eq(Optional.of(endDate)));
        }
    }

    // ========== DYNAMIC ASSIGNMENT REPORT TESTS ==========

    @Test
    @DisplayName("Should generate dynamic assignment report successfully")
    void shouldGenerateDynamicAssignmentReportSuccessfully() throws IOException {
        // Given
        String sortField = "assignedDate";
        String sortOrder = "desc";
        String fileName = "assignment_report";
        List<Integer> statusIds = List.of(1, 3); // 1=Accepted, 3=Waiting for acceptance
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<Assignment> assignmentList = List.of(mockAssignment);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            // Mock assignment status lookups
            AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                    .id(1)
                    .name(AssignmentStatusType.ACCEPTED.getDbName())
                    .build();
            AssignmentStatus waitingStatus = AssignmentStatus.builder()
                    .id(3)
                    .name(AssignmentStatusType.WAITING_FOR_ACCEPTANCE.getDbName())
                    .build();
            
            when(assignmentStatusRepo.findById(1)).thenReturn(Optional.of(acceptedStatus));
            when(assignmentStatusRepo.findById(3)).thenReturn(Optional.of(waitingStatus));
            
            sortUtilMock.when(() -> SortUtil.buildAssignmentReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(assignmentRepo.findAll(ArgumentMatchers.<Specification<Assignment>>any(), eq(mockSort))).thenReturn(assignmentList);
            when(excelBuilder.build(any(), eq(fileName), eq("Assignment Report"), 
                    eq(Optional.of(startDate)), eq(Optional.of(endDate)))).thenReturn(mockExcelData);

            // When
            byte[] result = reportService.generateDynamicAssignmentReport(sortField, sortOrder, fileName, 
                    statusIds, startDate, endDate);

            // Then
            assertNotNull(result);
            assertArrayEquals(mockExcelData, result);

            verify(assignmentRepo).findAll(ArgumentMatchers.<Specification<Assignment>>any(), eq(mockSort));
            verify(excelBuilder).build(any(), eq(fileName), eq("Assignment Report"), 
                    eq(Optional.of(startDate)), eq(Optional.of(endDate)));
        }
    }

    @Test
    @DisplayName("Should handle IOException in dynamic assignment report")
    void shouldHandleIOExceptionInDynamicAssignmentReport() throws IOException {
        // Given
        String sortField = "assignedDate";
        String sortOrder = "desc";
        String fileName = "assignment_report";
        List<Integer> statusIds = List.of(1); // 1=Accepted
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<Assignment> assignmentList = List.of(mockAssignment);

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            // Mock assignment status lookup
            AssignmentStatus acceptedStatus = AssignmentStatus.builder()
                    .id(1)
                    .name(AssignmentStatusType.ACCEPTED.getDbName())
                    .build();
            
            when(assignmentStatusRepo.findById(1)).thenReturn(Optional.of(acceptedStatus));
            
            sortUtilMock.when(() -> SortUtil.buildAssignmentReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(assignmentRepo.findAll(ArgumentMatchers.<Specification<Assignment>>any(), eq(mockSort))).thenReturn(assignmentList);
            when(excelBuilder.build(any(), anyString(), anyString(), any(), any()))
                    .thenThrow(new IOException("Excel generation failed"));

            // When & Then
            assertThrows(IOException.class, () -> reportService.generateDynamicAssignmentReport(
                    sortField, sortOrder, fileName, statusIds, startDate, endDate));

            verify(assignmentRepo).findAll(ArgumentMatchers.<Specification<Assignment>>any(), eq(mockSort));
            verify(excelBuilder).build(any(), anyString(), anyString(), any(), any());
        }
    }

    // ========== EDGE CASES AND ERROR HANDLING ==========

    @Test
    @DisplayName("Should handle invalid sort parameters gracefully")
    void shouldHandleInvalidSortParametersGracefully() {
        // Given
        String invalidSortField = "invalid_field";
        String invalidSortOrder = "invalid_order";

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(invalidSortField, invalidSortOrder))
                    .thenThrow(new IllegalArgumentException("Invalid sort field"));

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                    () -> reportService.getReport(0, 20, invalidSortField, invalidSortOrder));

            sortUtilMock.verify(() -> SortUtil.buildAssetReportSort(invalidSortField, invalidSortOrder));
        }
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Given
        String sortField = "category";
        String sortOrder = "asc";

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(reportRepo.findAll(any(PageRequest.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThrows(RuntimeException.class, 
                    () -> reportService.getReport(0, 20, sortField, sortOrder));

            verify(reportRepo).findAll(any(PageRequest.class));
        }
    }

    @Test
    @DisplayName("Should handle large dataset efficiently")
    void shouldHandleLargeDatasetEfficiently() throws IOException {
        // Given
        String sortField = "category";
        String sortOrder = "asc";
        List<Report> largeReportList = new ArrayList<>();
        
        // Create a large dataset (1000 reports)
        for (int i = 0; i < 1000; i++) {
            Report report = new Report();
            report.setId(i);
            report.setCategory("Category " + i);
            report.setTotal(i * 10);
            largeReportList.add(report);
        }

        try (MockedStatic<SortUtil> sortUtilMock = mockStatic(SortUtil.class)) {
            sortUtilMock.when(() -> SortUtil.buildAssetReportSort(sortField, sortOrder)).thenReturn(mockSort);
            when(reportRepo.findAll(mockSort)).thenReturn(largeReportList);
            when(excelBuilder.build(eq(largeReportList), anyString(), anyString(), any(), any(), any()))
                    .thenReturn(mockExcelData);

            // When
            byte[] result = reportService.standardExport(sortField, sortOrder);

            // Then
            assertNotNull(result);
            assertArrayEquals(mockExcelData, result);

            verify(reportRepo).findAll(mockSort);
            verify(excelBuilder).build(eq(largeReportList), anyString(), anyString(), any(), any(), any());
        }
    }
}
