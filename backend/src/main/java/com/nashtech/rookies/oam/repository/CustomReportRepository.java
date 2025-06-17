package com.nashtech.rookies.oam.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CustomReportRepository {
    List<Map<String, Object>> getDynamicAssetReport(
            List<Long> categoryIds,
            List<String> states,
            LocalDate startDate,
            LocalDate endDate
    );

}
