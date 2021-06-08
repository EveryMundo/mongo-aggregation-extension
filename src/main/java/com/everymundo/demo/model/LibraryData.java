package com.everymundo.demo.model;

import java.util.List;

import com.everymundo.demo.domain.Author;
import com.everymundo.demo.domain.Book;

import lombok.Data;

/**
 * Additional class to hold the data needed to populate the DB on startup.
 */
@Data
public class LibraryData {

    private List<Author> authors;
    private List<Book> books;

}