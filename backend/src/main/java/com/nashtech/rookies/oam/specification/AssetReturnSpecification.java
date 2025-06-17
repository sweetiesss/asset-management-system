package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.AssetReturn;
import com.nashtech.rookies.oam.model.Assignment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssetReturnSpecification {
    private static final String FIELD_ASSIGNMENT = "assignment";
    private static final String FIELD_ASSET = "asset";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CREATED_BY = "createdBy";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_RETURNED_DATE = "returnedDate";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_ID = "id";

    public static Specification<AssetReturn> searchByAssetCodeNameOrRequester(String search) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(search)) return null;

            Join<AssetReturn, Assignment> assignmentJoin = root.join(FIELD_ASSIGNMENT);
            Join<Assignment, Asset> assetJoin = assignmentJoin.join(FIELD_ASSET);

            String like = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(assetJoin.get(FIELD_CODE)), like),
                    cb.like(cb.lower(assetJoin.get(FIELD_NAME)), like),
                    cb.like(cb.lower(root.get(FIELD_CREATED_BY)), like)
            );
        };
    }

    public static Specification<AssetReturn> filterByStates(List<String> states) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(states)) return null;
            return root.get(FIELD_STATE).in(states);
        };
    }

    public static Specification<AssetReturn> filterByReturnedDate(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_RETURNED_DATE), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_RETURNED_DATE), to));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<AssetReturn> filterByLocation(UUID locationId) {
        return (root, query, cb) -> {
            if (locationId == null) return null;

            Join<Object, Object> assignmentJoin = root.join(FIELD_ASSIGNMENT);
            Join<Object, Object> assetJoin = assignmentJoin.join(FIELD_ASSET);
            Join<Object, Object> locationJoin = assetJoin.join(FIELD_LOCATION);

            return cb.equal(locationJoin.get(FIELD_ID), locationId);
        };
    }

    public static Specification<AssetReturn> build(String search, List<String> states, LocalDate from, LocalDate to, UUID locationId) {
        return Specification
                .where(searchByAssetCodeNameOrRequester(search))
                .and(filterByStates(states))
                .and(filterByReturnedDate(from, to))
                .and(filterByLocation(locationId));
    }
}
