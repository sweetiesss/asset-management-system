package com.nashtech.rookies.oam.dto.api;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiGenericResponse<T>(
        boolean success,
        String message,
        T data
) {

}