package com.everymundo.demo.repository;

import com.everymundo.demo.domain.Book;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BookRepository extends ReactiveMongoRepository<Book, String> {
    
}
