package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSpecificationTest {

    @Mock
    Root<User> root;

    @SuppressWarnings("unchecked")
    CriteriaQuery<User> query = mock(CriteriaQuery.class);

    @Mock
    CriteriaBuilder cb;

    @Mock
    Join<Object, Object> joinRoles;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(root.join("roles")).thenReturn(joinRoles);
        when(joinRoles.get("name")).thenReturn(mock(Path.class));
    }

    @Test
    void hasRoles_shouldReturnNull_whenRoleNamesNullOrEmpty() {
        Specification<User> specNull = UserSpecification.hasRoles(null);
        Specification<User> specEmpty = UserSpecification.hasRoles(Collections.emptyList());

        assertNull(specNull.toPredicate(root, query, cb));
        assertNull(specEmpty.toPredicate(root, query, cb));
    }

    @Test
    void hasRoles_shouldBuildPredicate() {
        List<String> roles = Arrays.asList("Admin", "User");

        // Mock the CriteriaBuilder.In and its behavior
        @SuppressWarnings("unchecked")
        CriteriaBuilder.In<String> inClause = mock(CriteriaBuilder.In.class);
        when(cb.in(any(Path.class))).thenReturn(inClause);
        when(inClause.value(anyString())).thenReturn(inClause);

        // query.distinct(true) should be called
        when(query.distinct(true)).thenReturn(query);

        var predicate = UserSpecification.hasRoles(roles).toPredicate(root, query, cb);

        // Verify join and inClause behavior
        verify(root).join("roles");
        verify(cb).in(joinRoles.get("name"));
        for (String role : roles) {
            verify(inClause).value(role);
        }
        verify(query).distinct(true);

        assertEquals(inClause, predicate);
    }

    @Test
    void hasSameLocation_shouldReturnNull_whenLocationIdNull() {
        Specification<User> spec = UserSpecification.hasSameLocation(null);
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void hasSameLocation_shouldBuildPredicate() {
        UUID locationId = UUID.randomUUID();

        Path<Object> locationPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);

        when(root.get("location")).thenReturn(locationPath);
        when(locationPath.get("id")).thenReturn(idPath);

        Predicate predicate = mock(Predicate.class);
        when(cb.equal(idPath, locationId)).thenReturn(predicate);

        Predicate result = UserSpecification.hasSameLocation(locationId).toPredicate(root, query, cb);

        verify(root).get("location");
        verify(locationPath).get("id");
        verify(cb).equal(idPath, locationId);

        assertEquals(predicate, result);
    }

    @Test
    void searchByNameOrStaffCode_shouldReturnNull_whenSearchNullOrEmpty() {
        Specification<User> specNull = UserSpecification.searchByNameOrStaffCode(null);
        Specification<User> specEmpty = UserSpecification.searchByNameOrStaffCode("  ");

        assertNull(specNull.toPredicate(root, query, cb));
        assertNull(specEmpty.toPredicate(root, query, cb));
    }

    @Test
    void searchByNameOrStaffCode_shouldBuildPredicate_withFullNameConcat() {
        String search = "John";
        String likePattern = "%john%";

        @SuppressWarnings("unchecked")
        Root<User> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        // Mocks for paths
        Path<String> firstNamePath = mock(Path.class);
        Path<String> lastNamePath = mock(Path.class);
        Path<String> staffCodePath = mock(Path.class);
        Path<String> lowerFirstName = mock(Path.class);
        Path<String> lowerLastName = mock(Path.class);
        Path<String> lowerStaffCode = mock(Path.class);
        Path<String> concat1 = mock(Path.class);
        Path<String> fullName = mock(Path.class);

        // Allow get("...") multiple times
        when(root.<String>get("firstName")).thenReturn(firstNamePath);
        when(root.<String>get("lastName")).thenReturn(lastNamePath);
        when(root.<String>get("staffCode")).thenReturn(staffCodePath);

        // Lowercase mocks
        when(cb.lower(firstNamePath)).thenReturn(lowerFirstName);
        when(cb.lower(lastNamePath)).thenReturn(lowerLastName);
        when(cb.lower(staffCodePath)).thenReturn(lowerStaffCode);

        // Concat + lower(fullName)
        when(cb.concat(firstNamePath, " ")).thenReturn(concat1);
        when(cb.concat(concat1, lastNamePath)).thenReturn(fullName);
        when(cb.lower(fullName)).thenReturn(fullName);

        // like predicates
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate p3 = mock(Predicate.class);
        Predicate p4 = mock(Predicate.class);
        when(cb.like(lowerFirstName, likePattern)).thenReturn(p1);
        when(cb.like(lowerLastName, likePattern)).thenReturn(p2);
        when(cb.like(lowerStaffCode, likePattern)).thenReturn(p3);
        when(cb.like(fullName, likePattern)).thenReturn(p4);

        // OR predicate
        Predicate finalPredicate = mock(Predicate.class);
        when(cb.or(p1, p2, p3, p4)).thenReturn(finalPredicate);

        // Execute
        Specification<User> spec = UserSpecification.searchByNameOrStaffCode(search);
        Predicate result = spec.toPredicate(root, query, cb);

        // Verify no unnecessary interactions
        verify(root, atLeastOnce()).get("firstName");
        verify(root, atLeastOnce()).get("lastName");
        verify(root).get("staffCode");

        verify(cb).lower(firstNamePath);
        verify(cb).lower(lastNamePath);
        verify(cb).lower(staffCodePath);
        verify(cb).concat(firstNamePath, " ");
        verify(cb).concat(concat1, lastNamePath);
        verify(cb).lower(fullName);

        verify(cb).like(lowerFirstName, likePattern);
        verify(cb).like(lowerLastName, likePattern);
        verify(cb).like(lowerStaffCode, likePattern);
        verify(cb).like(fullName, likePattern);
        verify(cb).or(p1, p2, p3, p4);

        assertEquals(finalPredicate, result);
    }

    @Test
    void excludeUser_shouldReturnNull_whenUserIdNull() {
        Specification<User> spec = UserSpecification.excludeUser(null);
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void excludeUser_shouldBuildPredicate() {
        UUID userId = UUID.randomUUID();
        Path<Object> idPath = mock(Path.class);

        when(root.get("id")).thenReturn(idPath);

        Predicate predicate = mock(Predicate.class);
        when(cb.notEqual(idPath, userId)).thenReturn(predicate);

        Predicate result = UserSpecification.excludeUser(userId).toPredicate(root, query, cb);

        verify(root).get("id");
        verify(cb).notEqual(idPath, userId);

        assertEquals(predicate, result);
    }



    @Test
    void build_shouldChainSpecifications() {
        String search = "jane";
        List<String> roles = Arrays.asList("Admin");
        UUID excludeUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        String sortField = "type";
        List<UserStatus> status = Arrays.asList(UserStatus.ACTIVE, UserStatus.INACTIVE);

        Specification<User> spec = UserSpecification.build(search, roles, excludeUserId, locationId, sortField,status);

        when(root.join("roles")).thenReturn(joinRoles);
        when(cb.in(any(Path.class))).thenReturn(mock(CriteriaBuilder.In.class));
        when(joinRoles.get("name")).thenReturn(mock(Path.class));
        when(root.get("status")).thenReturn(mock(Path.class));

        Path locationPath = mock(Path.class);
        Path locationIdPath = mock(Path.class);

        doReturn(locationPath).when(root).get("location");
        doReturn(locationIdPath).when(locationPath).get("id");

        @SuppressWarnings("unchecked")
        CriteriaQuery<User> queryTyped = (CriteriaQuery<User>) query;

        when(queryTyped.distinct(true)).thenReturn(queryTyped);
        when(queryTyped.distinct(false)).thenReturn(queryTyped);

        var predicate = spec.toPredicate(root, queryTyped, cb);
        assertNotNull(predicate);
    }


}
