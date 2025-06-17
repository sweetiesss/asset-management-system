package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.pagination.PageableRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssignmentPageRequest extends PageableRequest {
    private List<String> states;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate assignedDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate assignedDateTo;
    private UUID userId;
}
