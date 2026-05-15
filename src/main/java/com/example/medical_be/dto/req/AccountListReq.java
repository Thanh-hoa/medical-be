package com.example.medical_be.dto.req;


import com.example.medical_be.support.constant.CommonConstant;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
// import io.swagger.v3.oas.annotations.Parameter;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AccountListReq(
        //@Parameter(name = "page") 
        Integer page,
        //@Parameter(name = "limit") 
        Integer limit,
        // @Parameter(name = "q") 
        String q,
        // @Parameter(name = "sort_by") 
        String sortBy,
        // @Parameter(name = "order_by") 
        String orderBy) {
    public AccountListReq {
        page = (page != null) ? page : CommonConstant.DEFAULT_PAGE;
        limit = (limit != null) ? limit : CommonConstant.DEFAULT_LIMIT;
    }
}