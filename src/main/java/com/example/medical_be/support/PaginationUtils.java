package com.example.medical_be.support; 

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PaginationUtils {

    private PaginationUtils() { }

    public static int normalizePage(Integer pageNumber, IMessageTranslator messageTranslator) {
        int page = (pageNumber != null) ? pageNumber : CommonConstant.DEFAULT_PAGE;
        if (page <= 0) {
            throw new ApplicationException(messageTranslator.getMessage("page.min.1"));
        }
        return page - 1;
    }

    public static int normalizeLimit(Integer limitNumber, IMessageTranslator messageTranslator) {
        int limit = (limitNumber != null) ? limitNumber : CommonConstant.DEFAULT_LIMIT;
        if (limit <= 0) {
            throw new ApplicationException(messageTranslator.getMessage("limit.min.1"));
        }
        return limit;
    }

    public static Sort buildSort(String sortBy, 
        String orderBy, 
        Map<String, String> mapping, 
        String defaultProperty) {
        String sortProperty = defaultProperty;
        if (sortBy != null && !sortBy.isBlank() && mapping != null && !mapping.isEmpty()) {
            String key = sortBy.toLowerCase();
            sortProperty = mapping.getOrDefault(key, defaultProperty);
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(orderBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortProperty);
    }

    public static <T> PagedResponse<T> buildEmptyPagedResponse(Integer page, int limit) {
        return PagedResponse.<T>builder()
                .items(new ArrayList<>())
                .totalPage(0)
                .currentPage(page != null ? page : CommonConstant.DEFAULT_PAGE)
                .totalItems(0L)
                .limit(limit)
                .build();
    }

    public static <T> PagedResponse<T> buildPagedResponse(
            List<T> items,
            Page<?> pageRes,
            Integer requestedPage,
            int limit) {
        return PagedResponse.<T>builder()
                .items(items)
                .totalPage(pageRes.getTotalPages())
                .currentPage(requestedPage != null ? requestedPage : CommonConstant.DEFAULT_PAGE)
                .totalItems(pageRes.getTotalElements())
                .limit(limit)
                .build();
    }
}
