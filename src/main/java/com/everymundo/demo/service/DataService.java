package com.everymundo.demo.service;

import com.everymundo.demo.model.LibraryData;
import com.everymundo.demo.repository.AuthorRepository;
import com.everymundo.demo.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DataService {

    private final ObjectMapper mapper;
    private final ResourceLoader resourceLoader;

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;


    public Mono<Void> populateData() throws Exception {
        LibraryData data = mapper.readerFor(LibraryData.class).readValue(resourceLoader.getResource("classpath:db/data.json").getInputStream());

        Mono<Boolean> authorsMono = authorRepository.deleteAll()
                .thenMany(authorRepository.saveAll(data.getAuthors()))
                .then(Mono.just(true));
        Mono<Boolean> booksMono = bookRepository.deleteAll()
                .thenMany(bookRepository.saveAll(data.getBooks()))
                .then(Mono.just(true));

        return Mono.zip(authorsMono, booksMono)
                .then();
    }
    
}
