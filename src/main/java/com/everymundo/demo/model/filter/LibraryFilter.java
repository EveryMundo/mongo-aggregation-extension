package com.everymundo.demo.model.filter;

import lombok.Data;

@Data
public class LibraryFilter {

    private BookFilter book;
    private AuthorFilter author;


    public boolean hasBookFilters() {
        return book != null && book.hasFilters();
    }

    public boolean hasAuthorFilter() {
        return author != null && author.hasFilters();
    }
    
}
