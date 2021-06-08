package com.everymundo.demo.repository;

import com.everymundo.demo.domain.Author;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuthorRepository extends ReactiveMongoRepository<Author, String> {
    
}
