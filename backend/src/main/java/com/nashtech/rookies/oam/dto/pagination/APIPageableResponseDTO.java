package com.nashtech.rookies.oam.dto.pagination;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class APIPageableResponseDTO<T> implements Serializable {
    public List<T> content;
    public APIPageableDTO pageable;


    public APIPageableResponseDTO(Page<T> page) {
        setContent(page.getContent());
        setPageable(new APIPageableDTO(page));
    }
}