package com.everymundo.demo.model.filter;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class BookFilter {

    private String name;
    private Integer year;

    
    public boolean hasFilters() {
        return StringUtils.isNotBlank(name) || year != null;
    }

}
