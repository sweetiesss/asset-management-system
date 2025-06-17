package com.nashtech.rookies.oam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Integer id;
    private String name;
    private String prefix;
}
