package com.nashtech.rookies.oam.util;

import com.nashtech.rookies.oam.model.Report;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nashtech.rookies.oam.constant.SortConstants.*;
import static com.nashtech.rookies.oam.constant.UserConstants.USER_SORT_FIELD_MAPPING;

public class SortUtil {

    private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("asc", "desc");
    private static final Set<String> ALLOWED_ASSET_SORT_FIELDS = Set.of("code", "name", "categoryName", "state");
    public static final Map<String, String> ASSET_RETURN_SORT_FIELD_MAPPING = Map.of(
            "assetCode", "assignment.asset.code",
            "assetName", "assignment.asset.name",
            "createdBy", "createdBy",
            "assignedDate", "assignment.assignedDate",
            "updatedBy", "updatedBy",
            "returnedDate", "returnedDate",
            "state", "state"
    );



    private static final Set<String> ALLOWED_ASSET_REPORT_FIELDS =
            Set.of("category", "total", "assigned", "available", "notAvailable", "waitingForRecycling", "recycled");

    private static final Set<String> ALLOWED_USER_REPORT_FIELDS =
            Set.of("username");

    private static final Set<String> ALLOWED_ASSIGNMENT_REPORT_FIELDS =
            Set.of("id");

    public static Sort buildUserSort(String sortField, String sortOrder) {
        String mappedField = USER_SORT_FIELD_MAPPING.getOrDefault(
                sortField,
                DEFAULT_USER_LIST_SORT_FIELD
        );

        Sort.Direction direction = DESC.equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (SORT_FIELD_FULL_NAME.equals(sortField)) {
            return Sort.by(
                    new Sort.Order(direction, SORT_FIELD_FIRST_NAME),
                    new Sort.Order(direction, SORT_FIELD_LAST_NAME)
            );
        }

        return Sort.by(new Sort.Order(direction, mappedField));
    }

    public static Sort buildAssetSort(String sortField, String sortOrder) {
        if (!ALLOWED_ASSET_SORT_FIELDS.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }

        Sort.Direction direction = DESC.equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(new Sort.Order(direction, sortField));
    }

    public static Sort buildAssignmentSort(String sortField, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String sortBy;
        if (SORT_FIELD_USER_ID.equalsIgnoreCase(sortField)) {
            sortBy = SORT_FIELD_USER_ID_PATH;
        } else if (SORT_FIELD_CATEGORY_NAME.equalsIgnoreCase(sortField)) {
            sortBy = SORT_FIELD_CATEGORY_NAME_PATH;
        } else {
            sortBy = sortField;
        }

        return Sort.by(new Sort.Order(direction, sortBy));
    }

    public static Sort buildAssetReturnSort(String sortField, String sortOrder) {
        String mappedField = ASSET_RETURN_SORT_FIELD_MAPPING.get(sortField);
        if (mappedField == null) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }
        if (!ALLOWED_SORT_DIRECTIONS.contains(sortOrder.toLowerCase())) {
            throw new IllegalArgumentException("Invalid sort order: " + sortOrder);
        }

        Sort.Direction direction = DESC.equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(new Sort.Order(direction, mappedField));
    }
    private static final Set<String> ALLOWED_REPORT_SORT_FIELDS = Stream.of(Report.class.getDeclaredFields()).filter(field -> field.getName() != "id").map(field -> field.getName())
            .collect(Collectors.toSet());

    public static Sort buildReportSort(String sortField, String sortOrder) {
        return buildSort(ALLOWED_REPORT_SORT_FIELDS, sortField, sortOrder);
    }

    public static Sort buildAssetReportSort(String sortField, String sortOrder) {
        return buildSort(ALLOWED_ASSET_REPORT_FIELDS, sortField, sortOrder);
    }

    public static Sort buildUserReportSort(String sortField, String sortOrder) {
        return buildSort(ALLOWED_USER_REPORT_FIELDS, sortField, sortOrder);
    }

    public static Sort buildAssignmentReportSort(String sortField, String sortOrder) {
        return buildSort(ALLOWED_ASSIGNMENT_REPORT_FIELDS, sortField, sortOrder);
    }

    private static Sort buildSort(Set<String> allowedFields, String sortField, String sortOrder) {
        if (!allowedFields.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }
        if (!ALLOWED_SORT_DIRECTIONS.contains(sortOrder.toLowerCase())) {
            throw new IllegalArgumentException("Invalid sort order: " + sortOrder);
        }

        Sort.Direction direction = DESC.equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(new Sort.Order(direction, sortField));
    }
}
