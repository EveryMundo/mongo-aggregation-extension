package com.everymundo.demo.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.everymundo.demo.handler.LibraryHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouteConfiguration {
    
    @Bean
    public RouterFunction<ServerResponse> libraryRoutes(LibraryHandler handler) {
        return route(POST("/books").and(accept(MediaType.APPLICATION_JSON)), handler::searchBooks);
	}

}
