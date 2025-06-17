package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.pagination.APIPageableResponseDTO;
import com.nashtech.rookies.oam.dto.response.ExportResponse;
import com.nashtech.rookies.oam.model.Report;
import com.nashtech.rookies.oam.service.ReportService;
import com.nashtech.rookies.oam.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE;
import static com.nashtech.rookies.oam.constant.AppConstants.DEFAULT_PAGE_SIZE;
import static com.nashtech.rookies.oam.constant.SortConstants.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Reports", description = "Endpoints for retrieving and exporting reports")
public class ReportController {

    @Value("${file.export.name.default}")
    @NonFinal
    String defaultFileName;
    ReportService reportService;

    @GetMapping("/assets")
    @Operation(summary = "Get report data with pagination", description = "Retrieves paginated report data showing asset statistics by category", responses = {
            @ApiResponse(responseCode = "200", description = "Report data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiGenericResponse<APIPageableResponseDTO<Report>>> getReport(
            @RequestParam(defaultValue = DEFAULT_PAGE, name = "page") Integer pageNo,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, name = "size") Integer pageSize,
            @RequestParam(defaultValue = DEFAULT_REPORT_LIST_SORT_FIELD, name = "sort") String sortField,
            @RequestParam(defaultValue = ASC, name = "sortOrder") String sortOrder) {
        return ResponseUtil.success(
                "Report data retrieved successfully",
                reportService.getReport(pageNo, pageSize, sortField, sortOrder));
    }

    @GetMapping("/assets/export")
    @Operation(summary = "Export report assets data to Excel", description = "Exports report assets data to an Excel file with optional filtering. "
            +
            "For custom exports: states and categoryIds default to 'ALL' (includes all values). " +
            "Use specific values to filter, or 'ALL' to include everything.", responses = {
                    @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<byte[]> exportAssets(
            @RequestParam(defaultValue = DEFAULT_REPORT_LIST_SORT_FIELD, name = "sort") String sortField,
            @RequestParam(defaultValue = DEFAULT_EXPORT_TYPE, name = "exportType") String exportType,
            @RequestParam(defaultValue = ASC, name = "sortOrder") String sortOrder,
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "categoryIds", required = false) List<Integer> categoryIds,
            @RequestParam(name = "states", required = false) List<String> states,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            ExportResponse exportResponse = reportService.exportAssets(
                    sortField, exportType, sortOrder, fileName, 
                    categoryIds, states, startDate, endDate, defaultFileName);

            return ResponseEntity.ok()
                    .headers(exportResponse.getHeaders())
                    .body(exportResponse.getData());
        } catch (IOException e) {
            log.error("Error occurred while exporting assets", e);
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid export type: {}", exportType, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/export")
    @Operation(summary = "Export user data to Excel", description = "Exports user data to an Excel file with optional filtering. "
            +
            "For custom exports: roleTypes default to 'ALL' (includes all values). " +
            "Use specific values to filter, or 'ALL' to include everything.", responses = {
                    @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<byte[]> exportUser(
            @RequestParam(defaultValue = DEFAULT_USER_REPORT_EXPORT_SORT_FIELD, name = "sort") String sortField,
            @RequestParam(defaultValue = DEFAULT_EXPORT_TYPE_NON_ASSETS, name = "exportType") String exportType,
            @RequestParam(defaultValue = ASC, name = "sortOrder") String sortOrder,
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "roleType", required = false) List<String> roleTypes,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ExportResponse exportResponse = reportService.exportUsers(
                    sortField, sortOrder, fileName, roleTypes, startDate, endDate, defaultFileName);

            return ResponseEntity.ok()
                    .headers(exportResponse.getHeaders())
                    .body(exportResponse.getData());
        } catch (IOException e) {
            log.error("Error occurred while exporting users", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/assignments/export")
    @Operation(summary = "Export report assignments data to Excel", description = "Exports report assignments data to an Excel file with optional filtering. "
            +
            "For custom exports: states default to 'ALL' (includes all values). " +
            "Use specific values to filter, or 'ALL' to include everything.", responses = {
                    @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<byte[]> exportAssignment(
            @RequestParam(defaultValue = DEFAULT_ASSIGNMENT_REPORT_EXPORT_SORT_FIELD, name = "sort") String sortField,
            @RequestParam(defaultValue = DEFAULT_EXPORT_TYPE_NON_ASSETS, name = "exportType") String exportType,
            @RequestParam(defaultValue = ASC, name = "sortOrder") String sortOrder,
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "statusIds", required = false) List<Integer> statusIds,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ExportResponse exportResponse = reportService.exportAssignments(
                    sortField, sortOrder, fileName, statusIds, startDate, endDate, defaultFileName);

            return ResponseEntity.ok()
                    .headers(exportResponse.getHeaders())
                    .body(exportResponse.getData());
        } catch (IOException e) {
            log.error("Error occurred while exporting assignments", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
