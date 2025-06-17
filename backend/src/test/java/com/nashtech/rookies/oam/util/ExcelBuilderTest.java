package com.nashtech.rookies.oam.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExcelBuilderTest {

    private ExcelBuilder excelBuilder;

    @BeforeEach
    void setUp() {
        excelBuilder = new ExcelBuilder();
    }

    // Test data classes
    static class TestAsset {
        private String assetCode;
        private String assetName;
        private String category;
        private LocalDate installedDate;
        private String state;
        private String location;
        private Integer quantity;
        private Double price;
        private Boolean isActive;

        public TestAsset(String assetCode, String assetName, String category, LocalDate installedDate,
                        String state, String location, Integer quantity, Double price, Boolean isActive) {
            this.assetCode = assetCode;
            this.assetName = assetName;
            this.category = category;
            this.installedDate = installedDate;
            this.state = state;
            this.location = location;
            this.quantity = quantity;
            this.price = price;
            this.isActive = isActive;
        }

        // Getters
        public String getAssetCode() { return assetCode; }
        public String getAssetName() { return assetName; }
        public String getCategory() { return category; }
        public LocalDate getInstalledDate() { return installedDate; }
        public String getState() { return state; }
        public String getLocation() { return location; }
        public Integer getQuantity() { return quantity; }
        public Double getPrice() { return price; }
        public Boolean getIsActive() { return isActive; }
    }

    static class TestUser {
        private String staffCode;
        private String firstName;
        private String lastName;
        private String username;
        private LocalDateTime joinedDate;
        private String type;

        public TestUser(String staffCode, String firstName, String lastName, String username,
                       LocalDateTime joinedDate, String type) {
            this.staffCode = staffCode;
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.joinedDate = joinedDate;
            this.type = type;
        }

        // Getters
        public String getStaffCode() { return staffCode; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getUsername() { return username; }
        public LocalDateTime getJoinedDate() { return joinedDate; }
        public String getType() { return type; }
    }

    static class EmptyTestClass {
        // No fields
    }

    static class TestClassWithNulls {
        private String nullField;
        private Integer nullNumber;
        private Boolean nullBoolean;

        public TestClassWithNulls() {
            this.nullField = null;
            this.nullNumber = null;
            this.nullBoolean = null;
        }

        public String getNullField() { return nullField; }
        public Integer getNullNumber() { return nullNumber; }
        public Boolean getNullBoolean() { return nullBoolean; }
    }

    @Test
    void testBuildExcelWithValidAssetData() throws IOException {
        // Given
        List<TestAsset> assets = Arrays.asList(
            new TestAsset("AST001", "Laptop Dell", "IT Equipment", LocalDate.of(2023, 1, 15),
                         "Available", "HCM", 1, 1500.0, true),
            new TestAsset("AST002", "Monitor Samsung", "IT Equipment", LocalDate.of(2023, 2, 20),
                         "Assigned", "HN", 2, 300.0, true),
            new TestAsset("AST003", "Desk Chair", "Furniture", LocalDate.of(2023, 3, 10),
                         "Available", "DN", 1, 200.0, false)
        );

        // When
        byte[] result = excelBuilder.build(assets, "asset_report.xlsx", "Asset Report",
                                         Optional.of(LocalDate.of(2023, 1, 1)),
                                         Optional.of(LocalDate.of(2023, 12, 31)));

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals(1, workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            // Check header row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertTrue(headerRow.getLastCellNum() > 0);

            // Check data rows
            assertEquals(4, sheet.getLastRowNum() + 1); // Header + 3 data rows

            // Verify some data
            Row firstDataRow = sheet.getRow(1);
            assertNotNull(firstDataRow);
            assertEquals("AST001", firstDataRow.getCell(0).getStringCellValue());
        }
    }

    @Test
    void testBuildExcelWithValidUserData() throws IOException {
        // Given
        List<TestUser> users = Arrays.asList(
            new TestUser("SD001", "John", "Doe", "johndoe", LocalDateTime.of(2023, 1, 15, 9, 0), "STAFF"),
            new TestUser("SD002", "Jane", "Smith", "janesmith", LocalDateTime.of(2023, 2, 20, 10, 30), "ADMIN")
        );

        // When
        byte[] result = excelBuilder.build(users, "user_report.xlsx", "User Report",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            // Check we have header + 2 data rows
            assertEquals(3, sheet.getLastRowNum() + 1);

            // Verify some data
            Row firstDataRow = sheet.getRow(1);
            assertNotNull(firstDataRow);
            assertEquals("SD001", firstDataRow.getCell(0).getStringCellValue());
            assertEquals("John", firstDataRow.getCell(1).getStringCellValue());
        }
    }

    @Test
    void testBuildExcelWithEmptyList() throws IOException {
        // Given
        List<TestAsset> emptyList = new ArrayList<>();

        // When
        byte[] result = excelBuilder.build(emptyList, "empty_report.xlsx", "Empty Report",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);
            assertEquals(-1, sheet.getLastRowNum()); // Empty sheet returns -1 in Apache POI
        }
    }

    @Test
    void testBuildExcelWithNullList() throws IOException {
        // When
        byte[] result = excelBuilder.build(null, "null_report.xlsx", "Null Report",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);
            assertEquals(-1, sheet.getLastRowNum()); // Empty sheet returns -1 in Apache POI
        }
    }

    @Test
    void testBuildExcelWithEmptyClassNoFields() throws IOException {
        // Given
        List<EmptyTestClass> emptyClassList = Arrays.asList(new EmptyTestClass());

        // When
        byte[] result = excelBuilder.build(emptyClassList, "empty_class_report.xlsx", "Empty Class Report",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);
            assertEquals(-1, sheet.getLastRowNum()); // Empty sheet returns -1 in Apache POI (no accessible fields)
        }
    }

    @Test
    void testBuildExcelWithNullValues() throws IOException {
        // Given
        List<TestClassWithNulls> nullDataList = Arrays.asList(new TestClassWithNulls());

        // When
        byte[] result = excelBuilder.build(nullDataList, "null_values_report.xlsx", "Null Values Report",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            // Should have header + 1 data row
            assertEquals(2, sheet.getLastRowNum() + 1);

            // Check that null values are handled properly
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("", dataRow.getCell(0).getStringCellValue()); // null string becomes empty
            assertEquals("", dataRow.getCell(1).getStringCellValue()); // null number becomes empty
            assertEquals("", dataRow.getCell(2).getStringCellValue()); // null boolean becomes empty
        }
    }

    @Test
    void testBuildExcelWithMixedDataTypes() throws IOException {
        // Given
        List<TestAsset> mixedData = Arrays.asList(
            new TestAsset("AST001", "Test Asset", "Category", LocalDate.of(2023, 1, 15),
                         "Available", "Location", 5, 999.99, true),
            new TestAsset("AST002", "Another Asset", "Different Category", LocalDate.of(2023, 6, 30),
                         "Assigned", "Another Location", 0, 0.0, false)
        );

        // When
        byte[] result = excelBuilder.build(mixedData, "mixed_data_report.xlsx", "Mixed Data Report",
                                         Optional.of(LocalDate.of(2023, 1, 1)),
                                         Optional.of(LocalDate.of(2023, 12, 31)));

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);

            // Verify field name formatting in headers
            String firstHeaderValue = headerRow.getCell(0).getStringCellValue();
            assertNotNull(firstHeaderValue);
            assertTrue(firstHeaderValue.length() > 0);

            // Check data types are handled correctly
            Row firstDataRow = sheet.getRow(1);
            assertNotNull(firstDataRow);

            // String values
            assertEquals("AST001", firstDataRow.getCell(0).getStringCellValue());
            assertEquals("Test Asset", firstDataRow.getCell(1).getStringCellValue());

            // Check second row for different values
            Row secondDataRow = sheet.getRow(2);
            assertNotNull(secondDataRow);
            assertEquals("AST002", secondDataRow.getCell(0).getStringCellValue());
        }
    }

    @Test
    void testFieldNameFormatting() throws IOException {
        // This test indirectly tests the formatFieldName method through the Excel output
        // Given
        class TestFieldNames {
            private String simple_field;
            private String camelCaseField;
            private String UPPER_CASE_FIELD;
            private String mixedCase_Field;

            public TestFieldNames() {
                this.simple_field = "simple";
                this.camelCaseField = "camel";
                this.UPPER_CASE_FIELD = "upper";
                this.mixedCase_Field = "mixed";
            }

            public String getSimple_field() { return simple_field; }
            public String getCamelCaseField() { return camelCaseField; }
            public String getUPPER_CASE_FIELD() { return UPPER_CASE_FIELD; }
            public String getMixedCase_Field() { return mixedCase_Field; }
        }

        List<TestFieldNames> testData = Arrays.asList(new TestFieldNames());

        // When
        byte[] result = excelBuilder.build(testData, "field_names_test.xlsx", "Field Names Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);

            // Field names should be formatted properly
            assertTrue(headerRow.getLastCellNum() >= 4);
        }
    }

    @Test
    void testLargeDataSet() throws IOException {
        // Given
        List<TestAsset> largeDataSet = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeDataSet.add(new TestAsset(
                "AST" + String.format("%03d", i),
                "Asset " + i,
                "Category " + (i % 5),
                LocalDate.of(2023, (i % 12) + 1, (i % 28) + 1),
                i % 2 == 0 ? "Available" : "Assigned",
                "Location " + (i % 3),
                i % 10,
                100.0 + i,
                i % 2 == 0
            ));
        }

        // When
        byte[] result = excelBuilder.build(largeDataSet, "large_dataset.xlsx", "Large Dataset",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            // Should have header + 1000 data rows
            assertEquals(1001, sheet.getLastRowNum() + 1);

            // Verify first and last rows
            Row firstDataRow = sheet.getRow(1);
            assertNotNull(firstDataRow);
            assertEquals("AST000", firstDataRow.getCell(0).getStringCellValue());

            Row lastDataRow = sheet.getRow(1000);
            assertNotNull(lastDataRow);
            assertEquals("AST999", lastDataRow.getCell(0).getStringCellValue());
        }
    }

    @Test
    void testExcelStyling() throws IOException {
        // Given
        List<TestAsset> testData = Arrays.asList(
            new TestAsset("AST001", "Test Asset", "Category", LocalDate.of(2023, 1, 15),
                         "Available", "Location", 1, 100.0, true)
        );

        // When
        byte[] result = excelBuilder.build(testData, "styling_test.xlsx", "Styling Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel styling
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);

            // Check that header has styling applied
            Cell headerCell = headerRow.getCell(0);
            assertNotNull(headerCell);
            CellStyle headerStyle = headerCell.getCellStyle();
            assertNotNull(headerStyle);

            // Verify font is bold (header style should have bold font)
            Font font = workbook.getFontAt(headerStyle.getFontIndex());
            assertNotNull(font);
        }
    }

    @Test
    void testColumnAutoSizing() throws IOException {
        // Given
        List<TestAsset> testData = Arrays.asList(
            new TestAsset("AST001", "Very Long Asset Name That Should Trigger Column Resizing",
                         "Very Long Category Name", LocalDate.of(2023, 1, 15),
                         "Available", "Very Long Location Name", 1, 100.0, true)
        );

        // When
        byte[] result = excelBuilder.build(testData, "autosizing_test.xlsx", "Auto Sizing Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel was created successfully (auto-sizing is applied internally)
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertTrue(headerRow.getLastCellNum() > 0);

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("Very Long Asset Name That Should Trigger Column Resizing",
                        dataRow.getCell(1).getStringCellValue());
        }
    }

    @Test
    void testDateHandling() throws IOException {
        // Given
        class TestDates {
            private LocalDate localDate;
            private LocalDateTime localDateTime;
            private java.util.Date utilDate;

            public TestDates() {
                this.localDate = LocalDate.of(2023, 12, 25);
                this.localDateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
                this.utilDate = new java.util.Date();
            }

            public LocalDate getLocalDate() { return localDate; }
            public LocalDateTime getLocalDateTime() { return localDateTime; }
            public java.util.Date getUtilDate() { return utilDate; }
        }

        List<TestDates> testData = Arrays.asList(new TestDates());

        // When
        byte[] result = excelBuilder.build(testData, "dates_test.xlsx", "Dates Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);

            // LocalDate should be converted to string
            Cell localDateCell = dataRow.getCell(0);
            assertNotNull(localDateCell);
            assertEquals("2023-12-25", localDateCell.getStringCellValue());

            // LocalDateTime should be converted to string
            Cell localDateTimeCell = dataRow.getCell(1);
            assertNotNull(localDateTimeCell);
            assertTrue(localDateTimeCell.getStringCellValue().startsWith("2023-12-25T14:30:45"));

            // java.util.Date should be handled as date value
            Cell utilDateCell = dataRow.getCell(2);
            assertNotNull(utilDateCell);
        }
    }

    @Test
    void testSpecialCharactersInData() throws IOException {
        // Given
        List<TestAsset> specialCharsData = Arrays.asList(
            new TestAsset("AST@001", "Asset with Special!@#$%^&*()_+ Characters", "Category/\\|[]{}",
                         LocalDate.of(2023, 1, 15), "Available", "Location:;\"'<>?", 1, 100.0, true)
        );

        // When
        byte[] result = excelBuilder.build(specialCharsData, "special_chars_test.xlsx", "Special Chars Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content handles special characters
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("AST@001", dataRow.getCell(0).getStringCellValue());
            assertEquals("Asset with Special!@#$%^&*()_+ Characters", dataRow.getCell(1).getStringCellValue());
        }
    }

    @Test
    void testNumericDataTypes() throws IOException {
        // Given
        class TestNumbers {
            private Integer integerValue;
            private Long longValue;
            private Double doubleValue;
            private Float floatValue;

            public TestNumbers() {
                this.integerValue = 42;
                this.longValue = 123456789L;
                this.doubleValue = 3.14159;
                this.floatValue = 2.718f;
            }

            public Integer getIntegerValue() { return integerValue; }
            public Long getLongValue() { return longValue; }
            public Double getDoubleValue() { return doubleValue; }
            public Float getFloatValue() { return floatValue; }
        }

        List<TestNumbers> testData = Arrays.asList(new TestNumbers());

        // When
        byte[] result = excelBuilder.build(testData, "numbers_test.xlsx", "Numbers Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);

            // All numeric types should be stored as numeric values in Excel
            assertEquals(42.0, dataRow.getCell(0).getNumericCellValue(), 0.001);
            assertEquals(123456789.0, dataRow.getCell(1).getNumericCellValue(), 0.001);
            assertEquals(3.14159, dataRow.getCell(2).getNumericCellValue(), 0.001);
            assertEquals(2.718, dataRow.getCell(3).getNumericCellValue(), 0.001);
        }
    }

    @Test
    void testBooleanHandling() throws IOException {
        // Given
        class TestBooleans {
            private Boolean trueValue;
            private Boolean falseValue;
            private boolean primitiveTrue;
            private boolean primitiveFalse;

            public TestBooleans() {
                this.trueValue = true;
                this.falseValue = false;
                this.primitiveTrue = true;
                this.primitiveFalse = false;
            }

            public Boolean getTrueValue() { return trueValue; }
            public Boolean getFalseValue() { return falseValue; }
            public boolean getPrimitiveTrue() { return primitiveTrue; }
            public boolean getPrimitiveFalse() { return primitiveFalse; }
        }

        List<TestBooleans> testData = Arrays.asList(new TestBooleans());

        // When
        byte[] result = excelBuilder.build(testData, "booleans_test.xlsx", "Booleans Test",
                                         Optional.empty(), Optional.empty());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);

            // Boolean values should be stored as boolean values in Excel
            assertTrue(dataRow.getCell(0).getBooleanCellValue());
            assertFalse(dataRow.getCell(1).getBooleanCellValue());
            assertTrue(dataRow.getCell(2).getBooleanCellValue());
            assertFalse(dataRow.getCell(3).getBooleanCellValue());
        }
    }

    @Test
    void testBuildExcelWithFieldExclusion() throws IOException {
        // Given
        TestReport report = new TestReport(1, "Test Category", 100L, 50L, 30L, 10L, 5L, 5L);
        List<TestReport> reportList = List.of(report);

        // When - exclude the "id" field
        byte[] result = excelBuilder.build(reportList, "report_with_exclusion.xlsx", "Report",
                                         Optional.empty(), Optional.empty(), Set.of("id"));

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content - should not contain "id" column
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);
            assertTrue(sheet.getLastRowNum() > 0); // Should have at least header row

            // Check header row - should not contain "Id"
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            
            // Verify that "Id" is not in the headers
            boolean hasIdColumn = false;
            for (Cell cell : headerRow) {
                if ("Id".equals(cell.getStringCellValue())) {
                    hasIdColumn = true;
                    break;
                }
            }
            assertFalse(hasIdColumn, "Excel should not contain 'Id' column when id field is excluded");
            
            // Verify that other expected columns are present
            boolean hasCategoryColumn = false;
            for (Cell cell : headerRow) {
                if ("Category".equals(cell.getStringCellValue())) {
                    hasCategoryColumn = true;
                    break;
                }
            }
            assertTrue(hasCategoryColumn, "Excel should contain 'Category' column");
        }
    }

    @Test
    void testBuildExcelWithoutFieldExclusion() throws IOException {
        // Given
        TestReport report = new TestReport(1, "Test Category", 100L, 50L, 30L, 10L, 5L, 5L);
        List<TestReport> reportList = List.of(report);

        // When - do not exclude any fields
        byte[] result = excelBuilder.build(reportList, "report_no_exclusion.xlsx", "Report",
                                         Optional.empty(), Optional.empty(), Set.of());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify Excel content - should contain "id" column
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("AssetReport");
            assertNotNull(sheet);
            assertTrue(sheet.getLastRowNum() > 0); // Should have at least header row

            // Check header row - should contain "Id"
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            
            // Verify that "Id" is in the headers
            boolean hasIdColumn = false;
            for (Cell cell : headerRow) {
                if ("Id".equals(cell.getStringCellValue())) {
                    hasIdColumn = true;
                    break;
                }
            }
            assertTrue(hasIdColumn, "Excel should contain 'Id' column when no fields are excluded");
        }
    }

    // Test data class for Report
    static class TestReport {
        private int id;
        private String category;
        private long total;
        private long assigned;
        private long available;
        private long notAvailable;
        private long waitingForRecycling;
        private long recycled;

        public TestReport(int id, String category, long total, long assigned, long available,
                         long notAvailable, long waitingForRecycling, long recycled) {
            this.id = id;
            this.category = category;
            this.total = total;
            this.assigned = assigned;
            this.available = available;
            this.notAvailable = notAvailable;
            this.waitingForRecycling = waitingForRecycling;
            this.recycled = recycled;
        }
    }
}
