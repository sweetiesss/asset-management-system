package com.nashtech.rookies.oam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

/**
 * DTO for export responses containing both file data and HTTP headers
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportResponse {
    private byte[] data;
    private HttpHeaders headers;
    private String fileName;
    private String contentType;
}
