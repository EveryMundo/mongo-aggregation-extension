package com.everymundo.demo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document("books")
@Data
public class Book {

    @Id
    private String id;
    private String name;
    @Indexed
    private int year;
    @Indexed
    private String authorId;
    
}
