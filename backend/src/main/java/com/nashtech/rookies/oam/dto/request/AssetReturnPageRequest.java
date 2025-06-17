package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.pagination.PageableRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class AssetReturnPageRequest extends PageableRequest {
    private List<String> states;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnedDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnedDateTo;
}
