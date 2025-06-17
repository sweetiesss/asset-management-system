package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.Assignment;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignmentSpecificationTest {

    @Mock
    Root<Assignment> root;

    @Mock
    CriteriaBuilder cb;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    Join<Object, Object> assetJoin;

    @Mock
    Join<Object, Object> userJoin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchByAssetCodeNameOrAssignee_shouldReturnNull_whenSearchIsBlank() {
        assertNull(AssignmentSpecification.searchByAssetCodeNameOrAssignee(null).toPredicate(root, query, cb));
        assertNull(AssignmentSpecification.searchByAssetCodeNameOrAssignee("  ").toPredicate(root, query, cb));
    }

    @Test
    void searchByAssetCodeNameOrAssignee_shouldBuildPredicate() {
        String search = "Laptop";
        String like = "%laptop%";

        Join<Object, Object> assetJoin = mock(Join.class);
        Join<Object, Object> userJoin = mock(Join.class);

        Path<String> codePath = mock(Path.class);
        Path<String> namePath = mock(Path.class);
        Path<String> usernamePath = mock(Path.class);

        Expression<String> lowerCode = mock(Expression.class);
        Expression<String> lowerName = mock(Expression.class);
        Expression<String> lowerUsername = mock(Expression.class);

        // Mock predicates
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate p3 = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        // Set up joins
        when(root.join("asset")).thenReturn(assetJoin);
        when(root.join("user")).thenReturn(userJoin);

        // Set up path getters with correct type hints
        when(assetJoin.<String>get("code")).thenReturn(codePath);
        when(assetJoin.<String>get("name")).thenReturn(namePath);
        when(userJoin.<String>get("username")).thenReturn(usernamePath);

        // Set up lower(...) expressions
        when(cb.lower(codePath)).thenReturn(lowerCode);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.lower(usernamePath)).thenReturn(lowerUsername);

        // Set up like(...) predicates
        when(cb.like(lowerCode, like)).thenReturn(p1);
        when(cb.like(lowerName, like)).thenReturn(p2);
        when(cb.like(lowerUsername, like)).thenReturn(p3);

        // Set up or(...) predicate
        when(cb.or(p1, p2, p3)).thenReturn(orPredicate);

        // Execute
        Predicate result = AssignmentSpecification
                .searchByAssetCodeNameOrAssignee(search)
                .toPredicate(root, query, cb);

        // Assert
        assertEquals(orPredicate, result);
        verify(cb).or(p1, p2, p3);
    }

    @Test
    void filterByStates_shouldReturnNull_whenEmptyOrNull() {
        assertNull(AssignmentSpecification.filterByStates(null).toPredicate(root, query, cb));
        assertNull(AssignmentSpecification.filterByStates(Collections.emptyList()).toPredicate(root, query, cb));
    }

    @Test
    void filterByStates_shouldBuildPredicate() {
        List<String> states = Arrays.asList("Accepted", "Declined");

        // Mock path and subpath
        @SuppressWarnings("unchecked")
        Path<Object> statusPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> namePath = mock(Path.class);

        // status.get("name") -> namePath
        when(root.get("status")).thenReturn(statusPath);
        when(statusPath.<String>get("name")).thenReturn(namePath);

        // namePath.in(states) returns a predicate
        Predicate inPredicate = mock(Predicate.class);
        when(namePath.in(states)).thenReturn(inPredicate);

        // Call the actual method
        Predicate result = AssignmentSpecification
                .filterByStates(states)
                .toPredicate(root, query, cb);

        // Validate
        assertEquals(inPredicate, result);
    }

    @Test
    void filterByAssignedDate_AndUser_shouldReturnNull_whenAllParametersAreNull() {
        assertNull(AssignmentSpecification.filterByAssignedDateAndUser(null, null, null).toPredicate(root, query, cb));
    }

    @Test
    void filterByAssignedDateAndUser_shouldBuildAndPredicate_whenAllParametersProvided() {
        LocalDate fromDate = LocalDate.of(2025, 6, 1);
        LocalDate toDate = LocalDate.of(2025, 6, 30);
        UUID userId = UUID.randomUUID();

        Path<LocalDate> datePath = mock(Path.class);
        Path<Object> userPath = mock(Path.class);
        Path<UUID> userIdPath = mock(Path.class);

        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.<LocalDate>get("assignedDate")).thenReturn(datePath);
        when(root.get("user")).thenReturn(userPath);
        when(userPath.<UUID>get("id")).thenReturn(userIdPath);

        when(cb.greaterThanOrEqualTo(datePath, fromDate)).thenReturn(fromPredicate);
        when(cb.lessThanOrEqualTo(datePath, toDate)).thenReturn(toPredicate);
        when(cb.equal(userIdPath, userId)).thenReturn(userPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Predicate result = AssignmentSpecification
                .filterByAssignedDateAndUser(fromDate, toDate, userId)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
        verify(cb).greaterThanOrEqualTo(datePath, fromDate);
        verify(cb).lessThanOrEqualTo(datePath, toDate);
        verify(cb).equal(userIdPath, userId);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterByAssignedDateAndUser_shouldBuildAndPredicate_whenOnlyFromDateProvided() {
        LocalDate fromDate = LocalDate.of(2025, 6, 1);

        Path<LocalDate> datePath = mock(Path.class);
        Predicate fromPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.<LocalDate>get("assignedDate")).thenReturn(datePath);
        when(cb.greaterThanOrEqualTo(datePath, fromDate)).thenReturn(fromPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Predicate result = AssignmentSpecification
                .filterByAssignedDateAndUser(fromDate, null, null)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
        verify(cb).greaterThanOrEqualTo(datePath, fromDate);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterByAssignedDateAndUser_shouldBuildAndPredicate_whenOnlyToDateProvided() {
        LocalDate toDate = LocalDate.of(2025, 6, 30);

        Path<LocalDate> datePath = mock(Path.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.<LocalDate>get("assignedDate")).thenReturn(datePath);
        when(cb.lessThanOrEqualTo(datePath, toDate)).thenReturn(toPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Predicate result = AssignmentSpecification
                .filterByAssignedDateAndUser(null, toDate, null)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
        verify(cb).lessThanOrEqualTo(datePath, toDate);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterByAssignedDateAndUser_shouldBuildAndPredicate_whenOnlyUserIdProvided() {
        UUID userId = UUID.randomUUID();

        Path<Object> userPath = mock(Path.class);
        Path<UUID> userIdPath = mock(Path.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.get("user")).thenReturn(userPath);
        when(userPath.<UUID>get("id")).thenReturn(userIdPath);
        when(cb.equal(userIdPath, userId)).thenReturn(userPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Predicate result = AssignmentSpecification
                .filterByAssignedDateAndUser(null, null, userId)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
        verify(cb).equal(userIdPath, userId);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void filterByAssignedDate_shouldReturnNull_whenOnlyToDateAndUserProvided() {
        LocalDate toDate = LocalDate.of(2025, 6, 30);

        Predicate result = AssignmentSpecification
                .filterByAssignedDateAndUser(null, toDate, null)
                .toPredicate(root, query, cb);

        assertNull(result);
    }

    @Test
    void filterByAssignedDate_shouldReturnNull_whenOnlyFromDateAndUserProvided() {
        LocalDate fromDate = LocalDate.of(2025, 6, 1);

        Predicate result = AssignmentSpecification
                .filterByAssignedDateAndUser(fromDate, null, null)
                .toPredicate(root, query, cb);

        assertNull(result);
    }


    @Test
    void build_shouldChainSpecifications() {
        String search = "laptop";
        List<String> states = List.of("Accepted");
        LocalDate fromDate = LocalDate.of(2025, 6, 1);
        LocalDate toDate = LocalDate.of(2025, 6, 30);
        UUID userId = UUID.randomUUID();

        // Join mocks
        Join<Object, Object> assetJoin = mock(Join.class);
        Join<Object, Object> userJoin = mock(Join.class);

        when(root.join("asset")).thenReturn(assetJoin);
        when(root.join("user")).thenReturn(userJoin);

        // Mock Path<String> for search fields
        Path<String> codePath = mock(Path.class);
        Path<String> namePath = mock(Path.class);
        Path<String> usernamePath = mock(Path.class);

        when(assetJoin.<String>get("code")).thenReturn(codePath);
        when(assetJoin.<String>get("name")).thenReturn(namePath);
        when(userJoin.<String>get("username")).thenReturn(usernamePath);

        // Expressions for lower(...)
        Expression<String> lowerCode = mock(Expression.class);
        Expression<String> lowerName = mock(Expression.class);
        Expression<String> lowerUsername = mock(Expression.class);

        when(cb.lower(codePath)).thenReturn(lowerCode);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.lower(usernamePath)).thenReturn(lowerUsername);

        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate p3 = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(cb.like(lowerCode, "%laptop%")).thenReturn(p1);
        when(cb.like(lowerName, "%laptop%")).thenReturn(p2);
        when(cb.like(lowerUsername, "%laptop%")).thenReturn(p3);
        when(cb.or(p1, p2, p3)).thenReturn(orPredicate);

        Path<Object> statusPath = mock(Path.class);
        Path<String> statusNamePath = mock(Path.class);

        when(root.<Object>get("status")).thenReturn(statusPath);
        when(statusPath.<String>get("name")).thenReturn(statusNamePath);

        Predicate inPredicate = mock(Predicate.class);
        when(statusNamePath.in(states)).thenReturn(inPredicate);

        Path<LocalDate> datePath = mock(Path.class);
        Path<Object> userPath = mock(Path.class);
        Path<UUID> userIdPath = mock(Path.class);

        when(root.<LocalDate>get("assignedDate")).thenReturn(datePath);
        when(root.get("user")).thenReturn(userPath);
        when(userPath.<UUID>get("id")).thenReturn(userIdPath);

        // Mock date and user predicates
        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate datePredicate = mock(Predicate.class);

        when(cb.greaterThanOrEqualTo(datePath, fromDate)).thenReturn(fromPredicate);
        when(cb.lessThanOrEqualTo(datePath, toDate)).thenReturn(toPredicate);
        when(cb.equal(userIdPath, userId)).thenReturn(userPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(datePredicate);

        // Build final predicate
        Predicate finalPredicate = AssignmentSpecification
                .build(search, states, fromDate, toDate, userId, any())
                .toPredicate(root, query, cb);

        assertNotNull(finalPredicate);
    }

    @Test
    void build_shouldHandleNullParameters() {
        // Test with all null parameters
        Predicate result = AssignmentSpecification
                .build(null, null, null, null, null, null)
                .toPredicate(root, query, cb);

        // Since all specifications return null, the final result should be null
        assertNull(result);
    }

    @Test
    void build_shouldHandlePartialParameters() {
        String search = "laptop";

        // Mock search functionality
        Join<Object, Object> assetJoin = mock(Join.class);
        Join<Object, Object> userJoin = mock(Join.class);
        when(root.join("asset")).thenReturn(assetJoin);
        when(root.join("user")).thenReturn(userJoin);

        Path<String> codePath = mock(Path.class);
        Path<String> namePath = mock(Path.class);
        Path<String> usernamePath = mock(Path.class);
        when(assetJoin.<String>get("code")).thenReturn(codePath);
        when(assetJoin.<String>get("name")).thenReturn(namePath);
        when(userJoin.<String>get("username")).thenReturn(usernamePath);

        Expression<String> lowerCode = mock(Expression.class);
        Expression<String> lowerName = mock(Expression.class);
        Expression<String> lowerUsername = mock(Expression.class);
        when(cb.lower(codePath)).thenReturn(lowerCode);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.lower(usernamePath)).thenReturn(lowerUsername);

        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate p3 = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);
        when(cb.like(lowerCode, "%laptop%")).thenReturn(p1);
        when(cb.like(lowerName, "%laptop%")).thenReturn(p2);
        when(cb.like(lowerUsername, "%laptop%")).thenReturn(p3);
        when(cb.or(p1, p2, p3)).thenReturn(orPredicate);

        // Test with only search parameter (others are null)
        Predicate result = AssignmentSpecification
                .build(search, null, null, null, null, null)
                .toPredicate(root, query, cb);

        assertEquals(orPredicate, result);
    }
}