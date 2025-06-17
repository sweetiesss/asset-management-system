package com.nashtech.rookies.oam.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterOptionsResponse {
    private List<CategoryResponse> categories;
    private List<String> assetStates;
}
