package com.nashtech.rookies.oam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.api.ApiErrorResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ErrorResponseUtil {
    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, int status, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse body = ApiResult.error(errorCode, message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
