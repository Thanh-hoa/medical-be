package com.example.medical_be.dto.res;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Generic DTO for paginated list responses
 * 
 * @param <T> Type of items in the list
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagedResponse<T> {
    List<T> items;
    Integer currentPage;
    Integer limit;
    Long totalItems;
    Integer totalPage;
}
