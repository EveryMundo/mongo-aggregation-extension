package com.everymundo.demo.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.everymundo.demo.model.BookData;
import com.everymundo.demo.model.filter.LibraryFilter;
import com.everymundo.demo.service.LibraryService;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LibraryHandler {

    private final LibraryService libraryService;


    public Mono<ServerResponse> searchBooks(ServerRequest request) {
        return request.bodyToMono(LibraryFilter.class)
                .flatMap(filter -> ok().body(this.libraryService.searchBooks(filter), BookData.class));
    }

}
