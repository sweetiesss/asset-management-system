package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.response.ExportResponse;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.Report;
import com.nashtech.rookies.oam.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@DisplayName("ReportController Unit Tests")
class ReportControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private FirstLoginFilter firstLoginFilter;

    private Report mockReport;
    private APIPageableResponseDTO<Report> mockPageableResponse;
    private byte[] mockExcelData;
    private ExportResponse mockExportResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Mock data setup
        mockReport = new Report();
        mockReport.setId(1);
        mockReport.setCategory("Laptops");
        mockReport.setTotal(100);
        mockReport.setAssigned(50);
        mockReport.setAvailable(30);
        mockReport.setNotAvailable(10);
        mockReport.setWaitingForRecycling(5);
        mockReport.setRecycled(5);

        Page<Report> page = new PageImpl<>(List.of(mockReport), PageRequest.of(0, 20), 1);
        mockPageableResponse = new APIPageableResponseDTO<>(page);

        mockExcelData = "mock excel data".getBytes();
        
        // Setup mock ExportResponse
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.set("Content-Disposition", "attachment; filename=mock_export.xlsx");
        mockExportResponse = new ExportResponse(
            mockExcelData,
            headers,
            "mock_export.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    // ========== GET REPORT TESTS ==========

    @Test
    @DisplayName("Should get report data successfully with default parameters")
    @WithMockUser(roles = "ADMIN")
    void shouldGetReportWithDefaultParameters() throws Exception {
        // Given
        when(reportService.getReport(0, 20, "category", "asc")).thenReturn(mockPageableResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Report data retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].category").value("Laptops"))
                .andExpect(jsonPath("$.data.content[0].total").value(100));

        verify(reportService).getReport(0, 20, "category", "asc");
    }

    @Test
    @DisplayName("Should get report data with custom parameters")
    @WithMockUser(roles = "ADMIN")
    void shouldGetReportWithCustomParameters() throws Exception {
        // Given
        when(reportService.getReport(1, 10, "total", "desc")).thenReturn(mockPageableResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "total")
                        .param("sortOrder", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(reportService).getReport(1, 10, "total", "desc");
    }

    @Test
    @DisplayName("Should handle invalid page parameters gracefully")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInvalidPageParameters() throws Exception {
        // Given
        when(reportService.getReport(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockPageableResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets")
                        .param("page", "-1")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(reportService).getReport(anyInt(), anyInt(), anyString(), anyString());
    }

    // ========== EXPORT ASSETS TESTS ==========

    @Test
    @DisplayName("Should export standard report successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldExportStandardReport() throws Exception {
        // Given
        when(reportService.exportAssets(eq("category"), eq("standard"), eq("asc"), isNull(), isNull(), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "standard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes(mockExcelData));

        verify(reportService).exportAssets(eq("category"), eq("standard"), eq("asc"), isNull(), isNull(), isNull(), isNull(), isNull(), anyString());
    }

    @Test
    @DisplayName("Should export custom report successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldExportCustomReport() throws Exception {
        // Given
        List<Integer> categoryIds = Arrays.asList(1, 2);
        List<String> states = Arrays.asList("AVAILABLE", "ASSIGNED");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        when(reportService.exportAssets(
                eq("category"), eq("custom"), eq("asc"), 
                eq("custom_report"), eq(categoryIds), eq(states), eq(startDate), eq(endDate), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .param("fileName", "custom_report")
                        .param("categoryIds", "1,2")
                        .param("states", "AVAILABLE,ASSIGNED")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes(mockExcelData));

        verify(reportService).exportAssets(eq("category"), eq("custom"), eq("asc"), eq("custom_report"), eq(categoryIds), eq(states), eq(startDate), eq(endDate), anyString());
    }

    @Test
    @DisplayName("Should use default filename for custom export when fileName is null")
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultFilenameWhenFileNameIsNull() throws Exception {
        // Given
        when(reportService.exportAssets(
                anyString(), eq("custom"), anyString(), isNull(), isNull(), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        verify(reportService).exportAssets(anyString(), eq("custom"), anyString(), isNull(), isNull(), isNull(), isNull(), isNull(), anyString());
    }

    @Test
    @DisplayName("Should use default filename for custom export when fileName is blank")
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultFilenameWhenFileNameIsBlank() throws Exception {
        // Given
        when(reportService.exportAssets(
                anyString(), eq("custom"), anyString(), eq(""), isNull(), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .param("fileName", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        verify(reportService).exportAssets(anyString(), eq("custom"), anyString(), eq(""), isNull(), isNull(), isNull(), isNull(), anyString());
    }

    @Test
    @DisplayName("Should throw exception for invalid export type")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowExceptionForInvalidExportType() throws Exception {
        // Given
        when(reportService.exportAssets(anyString(), eq("invalid"), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid export type: invalid"));

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle IOException during export")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleIOExceptionDuringExport() throws Exception {
        // Given
        when(reportService.exportAssets(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenThrow(new IOException("Export failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "standard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(reportService).exportAssets(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString());
    }

    // ========== EXPORT USERS TESTS ==========

    @Test
    @DisplayName("Should export user report successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldExportUserReport() throws Exception {
        // Given
        List<String> roleTypes = Arrays.asList("ADMIN", "STAFF");
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        when(reportService.exportUsers(
                eq("username"), eq("asc"), eq("user_report"), 
                eq(roleTypes), eq(startDate), eq(endDate), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/users/export")
                        .param("fileName", "user_report")
                        .param("roleType", "ADMIN,STAFF")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes(mockExcelData));

        verify(reportService).exportUsers(eq("username"), eq("asc"), eq("user_report"), eq(roleTypes), eq(startDate), eq(endDate), anyString());
    }

    @Test
    @DisplayName("Should export user report with default parameters")
    @WithMockUser(roles = "ADMIN")
    void shouldExportUserReportWithDefaults() throws Exception {
        // Given
        when(reportService.exportUsers(
                eq("username"), eq("asc"), isNull(), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/users/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        verify(reportService).exportUsers(eq("username"), eq("asc"), isNull(), isNull(), isNull(), isNull(), anyString());
    }

    @Test
    @DisplayName("Should handle IOException during user export")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleIOExceptionDuringUserExport() throws Exception {
        // Given
        when(reportService.exportUsers(anyString(), anyString(), any(), any(), any(), any(), anyString()))
                .thenThrow(new IOException("User export failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/reports/users/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(reportService).exportUsers(anyString(), anyString(), any(), any(), any(), any(), anyString());
    }

    // ========== EXPORT ASSIGNMENTS TESTS ==========

    @Test
    @DisplayName("Should export assignment report successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldExportAssignmentReport() throws Exception {
        // Given
        List<Integer> statusIds = Arrays.asList(1, 3); // 1=Accepted, 3=Waiting for acceptance
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        when(reportService.exportAssignments(
                eq("id"), eq("asc"), eq("assignment_report"), 
                eq(statusIds), eq(startDate), eq(endDate), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assignments/export")
                        .param("fileName", "assignment_report")
                        .param("statusIds", "1,3")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes(mockExcelData));

        verify(reportService).exportAssignments(eq("id"), eq("asc"), eq("assignment_report"), eq(statusIds), eq(startDate), eq(endDate), anyString());
    }

    @Test
    @DisplayName("Should export assignment report with default parameters")
    @WithMockUser(roles = "ADMIN")
    void shouldExportAssignmentReportWithDefaults() throws Exception {
        // Given
        when(reportService.exportAssignments(
                eq("id"), eq("asc"), isNull(), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assignments/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        verify(reportService).exportAssignments(eq("id"), eq("asc"), isNull(), isNull(), isNull(), isNull(), anyString());
    }

    @Test
    @DisplayName("Should handle IOException during assignment export")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleIOExceptionDuringAssignmentExport() throws Exception {
        // Given
        when(reportService.exportAssignments(anyString(), anyString(), any(), any(), any(), any(), anyString()))
                .thenThrow(new IOException("Assignment export failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assignments/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(reportService).exportAssignments(anyString(), anyString(), any(), any(), any(), any(), anyString());
    }

    // ========== AUTHORIZATION TESTS ==========

    @Test
    @DisplayName("Should require authentication for getting reports")
    void shouldRequireAuthenticationForGetReports() throws Exception {
        mockMvc.perform(get("/api/v1/reports/assets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require authentication for exporting assets")
    void shouldRequireAuthenticationForExportAssets() throws Exception {
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require authentication for exporting users")
    void shouldRequireAuthenticationForExportUsers() throws Exception {
        mockMvc.perform(get("/api/v1/reports/users/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require authentication for exporting assignments")
    void shouldRequireAuthenticationForExportAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/reports/assignments/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== PARAMETER VALIDATION TESTS ==========

    @Test
    @DisplayName("Should handle invalid date format")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .param("startDate", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle valid date formats")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleValidDateFormats() throws Exception {
        // Given
        when(reportService.exportAssets(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle single category ID")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleSingleCategoryId() throws Exception {
        // Given
        when(reportService.exportAssets(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .param("categoryIds", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle multiple category IDs")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleMultipleCategoryIds() throws Exception {
        // Given
        when(reportService.exportAssets(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockExportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/assets/export")
                        .param("exportType", "custom")
                        .param("categoryIds", "1,2,3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
