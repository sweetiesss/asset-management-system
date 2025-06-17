package com.nashtech.rookies.oam.dto.pagination;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

@Data
public class APIPageableDTO implements Serializable {
    int pageNumber;
    int pageSize;
    int offset;
    int numberOfElements;
    long totalElements;
    int totalPages;
    boolean sorted;
    boolean first;
    boolean last;
    boolean empty;

    public <T> APIPageableDTO(Page<T> page) {
        Pageable pageable = page.getPageable();
        setPageNumber((pageable.isPaged()) ? pageable.getPageNumber() : 0);
        setPageSize((pageable.isPaged()) ? pageable.getPageSize() : page.getNumberOfElements());
        setTotalElements(page.getTotalElements());
        setTotalPages(page.getTotalPages());
        setNumberOfElements(page.getNumberOfElements());
        setSorted(page.getSort().isSorted());
        setFirst(page.isFirst());
        setLast(page.isLast());
        setEmpty(page.isEmpty());
    }


}
