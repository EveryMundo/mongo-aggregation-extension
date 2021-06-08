package com.everymundo.demo.model.filter;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class AuthorFilter {

    private String firstName;
    private String middleName;
    private String lastName;


    public boolean hasFilters() {
        return !StringUtils.isAllBlank(firstName, middleName, lastName);
    }

}
