package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.response.ExportResponse;
import com.nashtech.rookies.oam.dto.response.ReportResponse;
import com.nashtech.rookies.oam.model.Report;
import com.nashtech.rookies.oam.repository.*;
import com.nashtech.rookies.oam.service.ReportService;
import com.nashtech.rookies.oam.specification.AssetSpecification;
import com.nashtech.rookies.oam.specification.AssignmentSpecification;
import com.nashtech.rookies.oam.specification.UserSpecification;
import com.nashtech.rookies.oam.util.ExcelBuilder;
import com.nashtech.rookies.oam.util.SortUtil;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ReportServiceImpl implements ReportService {
    ReportRepository reportRepo;
    AssetRepository assetRepo;
    UserRepository userRepo;
    AssignmentRepository assignmentRepo;
    AssignmentStatusRepository assignmentStatusRepo;
    ExcelBuilder excelBuilder;

    @Override
    public APIPageableResponseDTO<Report> getReport(int pageNo, int pageSize, String sortField,
            String sortOrder) {
        log.debug("Fetching report with pageNo: {}, pageSize: {}, sortField: {}, sortOrder: {}", pageNo,
                pageSize,
                sortField, sortOrder);
        int validPageNo = Math.max(0, pageNo);
        int validPageSize = Math.max(1, pageSize);
        Sort sort = SortUtil.buildAssetReportSort(sortField, sortOrder);
        Pageable pageable = PageRequest.of(validPageNo, validPageSize, sort);
        var reportPage = reportRepo.findAll(pageable);
        return new APIPageableResponseDTO<>(reportPage);
    }

    @Override
    public byte[] standardExport(String sortField, String sortOrder) throws IOException {
        log.debug("Exporting all reports with sortField: {}, sortOrder: {}", sortField, sortOrder);
        Sort sort = SortUtil.buildAssetReportSort(sortField, sortOrder);
        List<Report> allReports = reportRepo.findAll(sort);
        return this.excelBuilder.build(allReports, "Report_Export", "Standard Asset Report",
                Optional.empty(), Optional.empty(), Set.of("id"));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDynamicAssetReport(String sortField, String sortOrder, String fileName,
            List<Integer> categoryIds, List<String> states, LocalDate startDate, LocalDate endDate)
            throws IOException {
        var assetSpec = AssetSpecification.findByCriteria(categoryIds, states, startDate, endDate);
        Sort sort = SortUtil.buildAssetReportSort(sortField, sortOrder);
        var reportAsset = assetRepo.findAll(assetSpec, sort);
        var finalReportAsset = reportAsset.stream().map(ReportResponse.AssetReport::fromAsset).toList();
        return this.excelBuilder.build(finalReportAsset, fileName, "Asset Report", Optional.of(startDate),
                Optional.of(endDate));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDynamicUserReport(String sortField, String sortOrder, String fileName,
            List<String> roleTypes, LocalDate startDate, LocalDate endDate) throws IOException {
        log.debug(
                "Generating dynamic user report with sortField: {}, sortOrder: {}, fileName: {}, roleTypes: {}, startDate: {}, endDate: {}",
                sortField, sortOrder, fileName, roleTypes, startDate, endDate);
        var userSpec = UserSpecification.findByCriteria(roleTypes, startDate, endDate);
        Sort sort = SortUtil.buildUserReportSort(sortField, sortOrder);
        var reportUsers = userRepo.findAll(userSpec, sort);
        var finalReportUsers = reportUsers.stream().map(ReportResponse.UserReport::fromUser).toList();

        return this.excelBuilder.build(finalReportUsers, fileName, "User Report",
                Optional.of(startDate), Optional.of(endDate));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDynamicAssignmentReport(String sortField, String sortOrder, String fileName,
            List<Integer> statusIds, LocalDate startDate, LocalDate endDate) throws IOException {
        log.debug(
                "Generating dynamic assignment report with sortField: {}, sortOrder: {}, fileName: {}, startDate: {}, endDate: {}, statusIds: {}",
                sortField, sortOrder, fileName, statusIds, startDate, endDate);
        System.out.println("Status IDs: " + statusIds);
        var assignmentStatuses = statusIds.stream()
                .map(assignmentStatusRepo::findById)
                .map(Optional::get)
                .toList();
        var assignmentSpec = AssignmentSpecification.findByCriteria(assignmentStatuses, startDate, endDate);
        Sort sort = SortUtil.buildAssignmentReportSort(sortField, sortOrder);
        var reportAssignments = assignmentRepo.findAll(assignmentSpec, sort);
        var finalReportAssignments = reportAssignments.stream()
                .map(ReportResponse.AssignmentReport::fromAssignment).toList();

        return this.excelBuilder.build(finalReportAssignments, fileName, "Assignment Report",
                Optional.of(startDate), Optional.of(endDate));
    }

    @Override
    public String generateStandardExportFilename() {
        return String.format("standard_asset_export_%s", getReadableTimestamp());
    }

    @Override
    public String generateCustomExportFilename(String customFileName, String defaultFileName) {
        String finalFileName = Optional.ofNullable(customFileName)
                .filter(name -> !name.isBlank())
                .orElse(defaultFileName);
        return String.format("%s_%s", finalFileName, getReadableTimestamp());
    }

    private String getReadableTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }

    @Override
    public ExportResponse exportAssets(String sortField, String exportType, String sortOrder, String fileName,
                                     List<Integer> categoryIds, List<String> states, LocalDate startDate, LocalDate endDate,
                                     String defaultFileName) throws IOException {
        log.debug("Exporting assets with type: {}, sortField: {}, sortOrder: {}", exportType, sortField, sortOrder);
        
        byte[] excelData;
        String finalFileName;
        
        switch (exportType.toLowerCase()) {
            case "standard" -> {
                excelData = standardExport(sortField, sortOrder);
                finalFileName = generateStandardExportFilename();
            }
            case "custom" -> {
                excelData = generateDynamicAssetReport(sortField, sortOrder, fileName, categoryIds, states, startDate, endDate);
                finalFileName = generateCustomExportFilename(fileName, defaultFileName);
            }
            default -> throw new IllegalArgumentException("Invalid export type: " + exportType);
        }
        
        return createExportResponse(excelData, finalFileName);
    }

    @Override
    public ExportResponse exportUsers(String sortField, String sortOrder, String fileName,
                                    List<String> roleTypes, LocalDate startDate, LocalDate endDate,
                                    String defaultFileName) throws IOException {
        log.debug("Exporting users with sortField: {}, sortOrder: {}", sortField, sortOrder);
        
        byte[] excelData = generateDynamicUserReport(sortField, sortOrder, fileName, roleTypes, startDate, endDate);
        String finalFileName = generateCustomExportFilename(fileName, defaultFileName);
        
        return createExportResponse(excelData, finalFileName);
    }

    @Override
    public ExportResponse exportAssignments(String sortField, String sortOrder, String fileName,
                                          List<Integer> statusIds, LocalDate startDate, LocalDate endDate,
                                          String defaultFileName) throws IOException {
        log.debug("Exporting assignments with sortField: {}, sortOrder: {}", sortField, sortOrder);
        
        byte[] excelData = generateDynamicAssignmentReport(sortField, sortOrder, fileName, statusIds, startDate, endDate);
        String finalFileName = generateCustomExportFilename(fileName, defaultFileName);
        
        return createExportResponse(excelData, finalFileName);
    }

    private ExportResponse createExportResponse(byte[] data, String fileName) {
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", fileName + ".xlsx");
        
        return ExportResponse.builder()
                .data(data)
                .headers(headers)
                .fileName(fileName)
                .contentType(contentType)
                .build();
    }
}
