package com.nashtech.rookies.oam.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
@Slf4j
@Service
public class ExcelBuilder {
    private static final String DEFAULT_SHEET_NAME = "AssetReport";
    private final String ERROR_CELL_VALUE = "N/A";
    public ExcelBuilder() {
    }

    public <T> byte[] build(List<T> data, String fileName, String title, Optional<LocalDate> startDate, Optional<LocalDate> endDate) throws IOException {
        return build(data, fileName, title, startDate, endDate, Set.of());
    }

    public <T> byte[] build(List<T> data, String fileName, String title, Optional<LocalDate> startDate, Optional<LocalDate> endDate, Set<String> excludeFields) throws IOException {
        log.debug("Starting standard Excel build for Object data, file: {}, excluded fields: {}",
                fileName != null ? fileName : "unnamed", excludeFields);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(DEFAULT_SHEET_NAME);

            if (data == null || data.isEmpty()) {
                log.info("No data provided for Object list, returning empty Excel workbook");
                return convertWorkbookToBytes(workbook);
            }

            List<Field> accessibleFields = extractAccessibleFields(data.get(0), excludeFields);
            if (accessibleFields.isEmpty()) {
                log.warn("No accessible fields found in data class: {}", data.get(0).getClass().getSimpleName());
                return convertWorkbookToBytes(workbook);
            }

            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            createHeaderRowFromFields(headerRow, accessibleFields, headerStyle);
            populateDataRowsFromObjects(sheet, 1, data, accessibleFields);
            autoSizeColumns(sheet, accessibleFields.size());

            log.debug("Standard Excel build completed successfully with {} rows", data.size() + 1);
            return convertWorkbookToBytes(workbook);
        } catch (Exception e) {
            log.error("Error occurred while building standard Excel file: {}", e.getMessage(), e);
            throw new IOException("Failed to generate standard Excel file: " + e.getMessage(), e);
        }
    }
    


    private void createHeaderRowFromFields(Row headerRow, List<Field> fields, CellStyle headerStyle) {
        for (int i = 0; i < fields.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(formatFieldName(fields.get(i).getName()));
            cell.setCellStyle(headerStyle);
        }
    }

    private <T> void populateDataRowsFromObjects(XSSFSheet sheet, int startRow, List<T> data, List<Field> fields) {
        for (int i = 0; i < data.size(); i++) {
            Row dataRow = sheet.createRow(i + startRow);
            T item = data.get(i);
            for (int j = 0; j < fields.size(); j++) {
                Cell cell = dataRow.createCell(j);
                try {
                    Object value = fields.get(j).get(item);
                    setCellValue(cell, value);
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field '{}'", fields.get(j).getName());
                    cell.setCellValue(ERROR_CELL_VALUE);
                }
            }
        }
    }
    
    private <T> List<Field> extractAccessibleFields(T dataObject, Set<String> excludeFields) {
        List<Field> accessibleFields = new ArrayList<>();
        try {
            Field[] fields = dataObject.getClass().getDeclaredFields();
            for (Field field : fields) {
                // Skip excluded fields
                if (excludeFields != null && excludeFields.contains(field.getName())) {
                    log.debug("Excluding field '{}' from Excel export", field.getName());
                    continue;
                }
                
                try {
                    field.setAccessible(true);
                    accessibleFields.add(field);
                } catch (SecurityException e) {
                    log.warn("Cannot access field '{}' in class '{}': {}", field.getName(), dataObject.getClass().getSimpleName(), e.getMessage());
                }
            }
        } catch (SecurityException e) {
            log.error("Cannot access fields in class '{}': {}", dataObject.getClass().getSimpleName(), e.getMessage());
        }
        return accessibleFields;
    }

    private void autoSizeColumns(XSSFSheet sheet, int columnCount) {
        try {
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-size columns: {}", e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String formatFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        String[] parts = fieldName.split("_|(?=\\p{Upper})");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            result.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1).toLowerCase())
                  .append(" ");
        }
        return result.toString().trim();
    }

    private void setCellValue(Cell cell, Object value) {
        try {
            if (value == null) {
                cell.setCellValue("");
            } else if (value instanceof String) {
                cell.setCellValue(StringUtil.normalizeWhitespace((String) value));
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof java.util.Date) {
                cell.setCellValue((java.util.Date) value);
            } else if (value instanceof java.time.LocalDate) {
                cell.setCellValue(value.toString());
            } else if (value instanceof java.time.LocalDateTime) {
                cell.setCellValue(value.toString());
            } else {
                cell.setCellValue(StringUtil.normalizeWhitespace(value.toString()));
            }
        } catch (Exception e) {
            log.warn("Failed to set cell value for type {}: {}", value != null ? value.getClass().getSimpleName() : "null", e.getMessage());
            cell.setCellValue("CONVERSION_ERROR");
        }
    }

    private byte[] convertWorkbookToBytes(XSSFWorkbook workbook) throws IOException {
        if (workbook == null) {
            throw new IllegalArgumentException("Workbook cannot be null");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to convert workbook to bytes: {}", e.getMessage(), e);
            throw new IOException("Failed to convert Excel workbook to byte array", e);
        }
    }
}