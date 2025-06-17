package com.nashtech.rookies.oam.specification;

import com.nashtech.rookies.oam.model.AssetReturn;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AssetReturnSpecificationTest {

    @Mock
    Root<AssetReturn> root;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    CriteriaBuilder cb;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchByAssetCodeNameOrRequester_shouldReturnNull_whenSearchBlank() {
        assertNull(AssetReturnSpecification.searchByAssetCodeNameOrRequester(null).toPredicate(root, query, cb));
        assertNull(AssetReturnSpecification.searchByAssetCodeNameOrRequester("   ").toPredicate(root, query, cb));
    }

    @Test
    void searchByAssetCodeNameOrRequester_shouldBuildPredicate() {
        String search = "Laptop";
        String like = "%laptop%";

        Join<Object, Object> assignmentJoin = mock(Join.class);
        Join<Object, Object> assetJoin = mock(Join.class);
        Path<String> codePath = mock(Path.class);
        Path<String> namePath = mock(Path.class);
        Path<String> createdByPath = mock(Path.class);
        Expression<String> lowerCode = mock(Expression.class);
        Expression<String> lowerName = mock(Expression.class);
        Expression<String> lowerCreatedBy = mock(Expression.class);
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate p3 = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.join("assignment")).thenReturn(assignmentJoin);
        when(assignmentJoin.join("asset")).thenReturn(assetJoin);
        when(assetJoin.<String>get("code")).thenReturn(codePath);
        when(assetJoin.<String>get("name")).thenReturn(namePath);
        when(root.<String>get("createdBy")).thenReturn(createdByPath);
        when(cb.lower(codePath)).thenReturn(lowerCode);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.lower(createdByPath)).thenReturn(lowerCreatedBy);
        when(cb.like(lowerCode, like)).thenReturn(p1);
        when(cb.like(lowerName, like)).thenReturn(p2);
        when(cb.like(lowerCreatedBy, like)).thenReturn(p3);
        when(cb.or(p1, p2, p3)).thenReturn(orPredicate);

        Predicate result = AssetReturnSpecification
                .searchByAssetCodeNameOrRequester(search)
                .toPredicate(root, query, cb);

        assertEquals(orPredicate, result);
        verify(cb).or(p1, p2, p3);
    }

    @Test
    void filterByStates_shouldReturnNull_whenEmptyOrNull() {
        assertNull(AssetReturnSpecification.filterByStates(null).toPredicate(root, query, cb));
        assertNull(AssetReturnSpecification.filterByStates(Collections.emptyList()).toPredicate(root, query, cb));
    }

    @Test
    void filterByStates_shouldReturnInPredicate() {
        List<String> states = List.of("Completed", "Waiting for returning");
        Path<String> statePath = mock(Path.class);
        Predicate inPredicate = mock(Predicate.class);

        when(root.<String>get("state")).thenReturn(statePath);
        when(statePath.in(states)).thenReturn(inPredicate);

        Predicate result = AssetReturnSpecification
                .filterByStates(states)
                .toPredicate(root, query, cb);

        assertEquals(inPredicate, result);
    }

    @Test
    void filterByReturnedDate_shouldReturnNull_whenBothDatesNull() {
        assertNull(AssetReturnSpecification.filterByReturnedDate(null, null).toPredicate(root, query, cb));
    }

    @Test
    void filterByReturnedDate_shouldReturnFromPredicate() {
        LocalDate from = LocalDate.of(2025, 6, 1);
        Path<LocalDate> datePath = mock(Path.class);
        Predicate fromPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.<LocalDate>get("returnedDate")).thenReturn(datePath);
        when(cb.greaterThanOrEqualTo(datePath, from)).thenReturn(fromPredicate);
        when(cb.and(fromPredicate)).thenReturn(andPredicate);

        Predicate result = AssetReturnSpecification
                .filterByReturnedDate(from, null)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
    }

    @Test
    void filterByReturnedDate_shouldReturnToPredicate() {
        LocalDate to = LocalDate.of(2025, 6, 30);
        Path<LocalDate> datePath = mock(Path.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.<LocalDate>get("returnedDate")).thenReturn(datePath);
        when(cb.lessThanOrEqualTo(datePath, to)).thenReturn(toPredicate);
        when(cb.and(toPredicate)).thenReturn(andPredicate);

        Predicate result = AssetReturnSpecification
                .filterByReturnedDate(null, to)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
    }

    @Test
    void filterByReturnedDate_shouldReturnAndPredicate_whenBothProvided() {
        LocalDate from = LocalDate.of(2025, 6, 1);
        LocalDate to = LocalDate.of(2025, 6, 30);
        Path<LocalDate> datePath = mock(Path.class);
        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.<LocalDate>get("returnedDate")).thenReturn(datePath);
        when(cb.greaterThanOrEqualTo(datePath, from)).thenReturn(fromPredicate);
        when(cb.lessThanOrEqualTo(datePath, to)).thenReturn(toPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Predicate result = AssetReturnSpecification
                .filterByReturnedDate(from, to)
                .toPredicate(root, query, cb);

        assertEquals(andPredicate, result);
    }

    @Test
    void filterByLocation_shouldReturnNull_whenLocationIdNull() {
        assertNull(AssetReturnSpecification.filterByLocation(null).toPredicate(root, query, cb));
    }

    @Test
    void filterByLocation_shouldReturnEqualPredicate() {
        UUID locationId = UUID.randomUUID();

        Join<Object, Object> assignmentJoin = mock(Join.class);
        Join<Object, Object> assetJoin = mock(Join.class);
        Join<Object, Object> locationJoin = mock(Join.class);
        Path<UUID> locationIdPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        when(root.join("assignment")).thenReturn(assignmentJoin);
        when(assignmentJoin.join("asset")).thenReturn(assetJoin);
        when(assetJoin.join("location")).thenReturn(locationJoin);
        when(locationJoin.<UUID>get("id")).thenReturn(locationIdPath);
        when(cb.equal(locationIdPath, locationId)).thenReturn(equalPredicate);

        Predicate result = AssetReturnSpecification
                .filterByLocation(locationId)
                .toPredicate(root, query, cb);

        assertEquals(equalPredicate, result);
    }

    @Test
    void build_shouldReturnNull_whenAllParamsNull() {
        Predicate result = AssetReturnSpecification
                .build(null, null, null, null, null)
                .toPredicate(root, query, cb);
        assertNull(result);
    }
}
