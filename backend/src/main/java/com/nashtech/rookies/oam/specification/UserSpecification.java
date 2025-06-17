package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSpecification {

    private static final String FIELD_ROLES = "roles";
    private static final String FIELD_ROLE_NAME = "name";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_ID = "id";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_LAST_NAME = "lastName";
    private static final String FIELD_STAFF_CODE = "staffCode";
    private static final String SORT_FIELD_TYPE = "type";
    private static final String FIELD_STATUS = "status";

    public static Specification<User> hasRoles(List<String> roleNames) {
        return (root, query, cb) -> {
            if (roleNames == null || roleNames.isEmpty()) return null;

            Join<Object, Object> roles = root.join(FIELD_ROLES);
            CriteriaBuilder.In<String> inClause = cb.in(roles.get(FIELD_ROLE_NAME));
            roleNames.forEach(inClause::value);

            if (query != null) {
                query.distinct(true);
            }
            return inClause;
        };
    }

    public static Specification<User> hasSameLocation(UUID locationId) {
        return (root, query, cb) -> locationId == null
                ? null
                : cb.equal(root.get(FIELD_LOCATION).get(FIELD_ID), locationId);
    }

    public static Specification<User> searchByNameOrStaffCode(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) return null;

            String like = "%" + search.toLowerCase() + "%";

            var fullName = cb.lower(cb.concat(cb.concat(root.get(FIELD_FIRST_NAME), " "), root.get(FIELD_LAST_NAME)));

            return cb.or(
                    cb.like(cb.lower(root.get(FIELD_FIRST_NAME)), like),
                    cb.like(cb.lower(root.get(FIELD_LAST_NAME)), like),
                    cb.like(cb.lower(root.get(FIELD_STAFF_CODE)), like),
                    cb.like(fullName, like)
            );
        };
    }

    public static Specification<User> excludeUser(UUID userId) {
        return (root, query, cb) -> userId == null
                ? null
                : cb.notEqual(root.get(FIELD_ID), userId);
    }

    public static Specification<User> includeStatuses(List<UserStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get(FIELD_STATUS).in(statuses);
        };
    }

    public static Specification<User> build(
            String search,
            List<String> roles,
            UUID excludeUserId,
            UUID locationId,
            String sortField,
            List<UserStatus> statuses
    ) {
        return Specification.where(searchByNameOrStaffCode(search))
                .and(hasRoles(roles))
                .and(excludeUser(excludeUserId))
                .and(hasSameLocation(locationId))
                .and(applySortJoins(sortField))
                .and(includeStatuses(statuses));
    }

    private static Specification<User> applySortJoins(String sortField) {
        return (root, query, cb) -> {
            if (SORT_FIELD_TYPE.equals(sortField)) {
                root.join(FIELD_ROLES, JoinType.LEFT);
                if (query != null) {
                    query.distinct(false);
                }
            }
            return null;
        };
    }

     public static Specification<User> findByCriteria(
            List<String> roleTypes,
            LocalDate startDate,
            LocalDate endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (roleTypes != null && !roleTypes.isEmpty()) {
                Join<User, Role> roleJoin = root.join("roles");
                predicates.add(roleJoin.get("name").in(roleTypes));
            }

            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("joinedOn"), startDate, endDate));
            }
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
