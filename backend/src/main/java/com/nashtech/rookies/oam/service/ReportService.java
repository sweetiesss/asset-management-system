package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.response.ExportResponse;
import com.nashtech.rookies.oam.model.Report;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    APIPageableResponseDTO<Report> getReport(
            int pageNo,
            int pageSize,
            String sortField,
            String sortOrder);

    // Legacy methods - keeping for backward compatibility but marked as deprecated
    @Deprecated
    byte[] standardExport(String sortField, String sortOrder) throws IOException;

    @Deprecated
    byte[] generateDynamicAssetReport(String sortField, String sortOrder, String fileName, List<Integer> categoryIds, List<String> states, LocalDate startDate, LocalDate endDate) throws IOException;

    @Deprecated
    byte[] generateDynamicUserReport(String sortField, String sortOrder, String fileName, List<String> roleTypes, LocalDate startDate, LocalDate endDate) throws IOException;

    @Deprecated
    byte[] generateDynamicAssignmentReport(String sortField, String sortOrder, String fileName, List<Integer> statusIds, LocalDate startDate, LocalDate endDate) throws IOException;

    // Legacy filename generation methods
    @Deprecated
    String generateStandardExportFilename();
    
    @Deprecated
    String generateCustomExportFilename(String customFileName, String defaultFileName);
    
    // New unified export methods
    ExportResponse exportAssets(String sortField, String exportType, String sortOrder, String fileName, 
                               List<Integer> categoryIds, List<String> states, LocalDate startDate, LocalDate endDate, 
                               String defaultFileName) throws IOException;
    
    ExportResponse exportUsers(String sortField, String sortOrder, String fileName, 
                              List<String> roleTypes, LocalDate startDate, LocalDate endDate,
                              String defaultFileName) throws IOException;
    
    ExportResponse exportAssignments(String sortField, String sortOrder, String fileName, 
                                   List<Integer> statusIds, LocalDate startDate, LocalDate endDate,
                                   String defaultFileName) throws IOException;
}
