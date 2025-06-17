package com.nashtech.rookies.oam.util;

import org.springframework.data.domain.PageRequest;

public class PageUtil {
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 1;

    private PageUtil() {
        // Prevent instantiation
    }

    public static PageRequest buildPageRequest(Integer pageNo, Integer pageSize) {
        int validPageNo = (pageNo != null && pageNo >= 0) ? pageNo : DEFAULT_PAGE_NUMBER;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
        return PageRequest.of(validPageNo, validPageSize);
    }
}
