package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.model.AssignmentStatus;
import com.nashtech.rookies.oam.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssignmentSpecification {

    private static final String FIELD_ASSET = "asset";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_USER = "user";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ASSIGNED_DATE = "assignedDate";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_ID = "id";

    public static Specification<Assignment> searchByAssetCodeNameOrAssignee(String search) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(search)) return null;
            String like = "%" + search.trim().toLowerCase() + "%";

            Join<Assignment, Asset> assetJoin = root.join(FIELD_ASSET);
            Join<Assignment, User> userJoin = root.join(FIELD_USER);

            return cb.or(
                    cb.like(cb.lower(assetJoin.get(FIELD_CODE)), like),
                    cb.like(cb.lower(assetJoin.get(FIELD_NAME)), like),
                    cb.like(cb.lower(userJoin.get(FIELD_USERNAME)), like)
            );
        };
    }

    public static Specification<Assignment> filterByStates(List<String> states) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(states)) return null;
            return root.get(FIELD_STATUS).get(FIELD_NAME).in(states);
        };
    }

    public static Specification<Assignment> filterByAssignedDateAndUser(LocalDate assignedDateFrom, LocalDate assignedDateTo, UUID userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (assignedDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_ASSIGNED_DATE), assignedDateFrom));
            }

            if (assignedDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_ASSIGNED_DATE), assignedDateTo));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get(FIELD_USER).get("id"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Assignment> inLocation(UUID locationId) {
        return (root, query, cb) -> {
            if (locationId == null) return null;

            Join<Assignment, Asset> assetJoin = root.join(FIELD_ASSET);
            return cb.equal(assetJoin.get(FIELD_LOCATION).get(FIELD_ID), locationId);
        };
    }

    public static Specification<Assignment> build(String search, List<String> states, LocalDate assignedDateFrom, LocalDate assignedDateTo , UUID userId, UUID locationId) {
        return Specification
                .where(searchByAssetCodeNameOrAssignee(search))
                .and(filterByStates(states))
                .and(filterByAssignedDateAndUser(assignedDateFrom, assignedDateTo, userId))
                .and(inLocation(locationId));
    }

    public static Specification<Assignment> findByCriteria(
            List<AssignmentStatus> statuses,
            LocalDate startDate,
            LocalDate endDate
        ) {
        return (root, query, criteriaBuilder) -> {
             List<Predicate> predicates = new ArrayList<>();
              if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
             if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("assignedDate"), startDate, endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
       
    }
}
