package com.everymundo.demo.model;

import lombok.Data;

@Data
public class BookData {

    private String name;
    private int year;
    private AuthorData author;
    
}
