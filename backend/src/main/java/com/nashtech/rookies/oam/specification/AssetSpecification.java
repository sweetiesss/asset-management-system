package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Location;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssetSpecification {
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_LOCATION_ID = "id";

    public static Specification<Asset> searchByCodeOrName(String search) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(search)) return null;
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get(FIELD_NAME)), like),
                    cb.like(cb.lower(root.get(FIELD_CODE)), like)
            );
        };
    }

    public static Specification<Asset> filterByCategories(List<String> categories) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(categories)) return null;
            Join<Object, Object> categoryJoin = root.join(FIELD_CATEGORY);
            return categoryJoin.get(FIELD_NAME).in(categories);
        };
    }

    public static Specification<Asset> filterByStates(List<String> states) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(states)) return null;
            return root.get(FIELD_STATE).in(states);
        };
    }

    public static Specification<Asset> inLocation(Location location) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_LOCATION).get(FIELD_LOCATION_ID), location.getId());
    }

    public static Specification<Asset> build(String search, List<String> categories, List<String> states, Location location) {
        return Specification.where(searchByCodeOrName(search))
                .and(filterByCategories(categories))
                .and(filterByStates(states))
                .and(inLocation(location));
    }

    public static Specification<Asset> findByCriteria(
            List<Integer> categoryIds,
            List<String> states,
            LocalDate startDate,
            LocalDate endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }

            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("installedDate"), startDate, endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

