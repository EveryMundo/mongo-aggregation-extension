# How to Extend Spring Data Reactive MongoDB Aggregation Features
<br/>

## 1. Overview
Spring Data’s mission is to provide a familiar and consistent, Spring-based programming model for data access while still retaining the special traits of the underlying data store. It makes it easy to use data access technologies, relational and non-relational databases, map-reduce frameworks, and cloud-based data services.<sup>[1]</sup>

The Spring Data MongoDB project provides integration with the MongoDB document database. Key functional areas of Spring Data MongoDB are a POJO centric model for interacting with a MongoDB DBCollection and easily writing a Repository style data access layer.<sup>[2]</sup>

Spring Data MongoDB offers a bunch of nice features, however, it does not support all the operations available in MongoDB. This tutorial will provide a clean way to extend Spring Data MongoDB aggregation features. We're not including all the supported operations. In case you need more or different ones that are not part of the tutorial, you can always follow the same approach.
<br/>

## 2. Getting Started
In this tutorial we're going to show you how easily extend Spring Data Reactive MongoDB aggregation features. You can start from scratch and complete each step or you can skip the steps that are already familiar to you and download the complete code [here](https://github.com/EveryMundo/mongo-aggregation-extension).

We'll be using Spring Boot 2.4.6, Spring Data Reactive MongoDB, Java 11, Apache Maven, MongoDB 4.4 and Docker. You can use slightly different versions for this tutorial but at least Java 8 and MongoDB 4.x are recommended. If you don't know all the technologies or if you're interested to take a deep dive any of them, you can check the following links:

* Java (https://docs.oracle.com/en/java/index.html)
* Spring Framework (https://spring.io/projects/spring-framework)
* Spring Boot (https://spring.io/projects/spring-boot)
* Spring Data Reactive MongoDB (https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive)
* Apache Maven (https://maven.apache.org)
* MongoDB (https://docs.mongodb.com/manual)
* Docker (https://docs.docker.com)

**Note:**<br/>
We recommend an OpenJDK distribution of Java for development as well as production environments since Oracle has changed the Licence Agreement (see here) starting April 16, 2019. AdoptOpenJDK (https://adoptopenjdk.net) offers free prebuilt OpenJDK binaries for Windows, Linux and MacOS.

This tutorial will implement an HTTP POST Book Search endpoint for a Library Service.
<br/>

### 2.1. Starting with Spring Initializr
Go to Spring Initializr (https://start.spring.io) to generate a new project with the required dependencies. Use the following configurations:

Alternatively you can use the following link:<br/>
https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.4.6.RELEASE&packaging=jar&jvmVersion=11&groupId=com.everymundo.demo&artifactId=mongo-aggregation-extension&name=mongo-aggregation-extension&description=MongoDB%20Demo%20Aggregations&packageName=com.everymundo.demo&dependencies=data-mongodb-reactive,webflux,lombok

Once ready, click the "**GENERATE**" button and wait for the download to complete. Unzip the project and open it with the IDE of your choice.

The following shows the `pom.xml` file created by Spring Initializr:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  
   <modelVersion>4.0.0</modelVersion>
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>2.4.6</version>
       <relativePath/>
       <!-- lookup parent from repository -->
   </parent>
  
   <groupId>com.everymundo.demo</groupId>
   <artifactId>mongo-aggregation-extension</artifactId>
   <version>0.0.1-SNAPSHOT</version>
   <name>mongo-aggregation</name>
   <description>MongoDB Demo Aggregations</description>
  
   <properties>
       <java.version>11</java.version>
   </properties>
  
   <dependencies>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-webflux</artifactId>
       </dependency>
 
       <dependency>
           <groupId>org.projectlombok</groupId>
           <artifactId>lombok</artifactId>
           <optional>true</optional>
       </dependency>
 
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-test</artifactId>
           <scope>test</scope>
       </dependency>
       <dependency>
           <groupId>io.projectreactor</groupId>
           <artifactId>reactor-test</artifactId>
           <scope>test</scope>
       </dependency>
   </dependencies>
 
   <build>
       <plugins>
           <plugin>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-maven-plugin</artifactId>
               <configuration>
                   <excludes>
                       <exclude>
                           <groupId>org.projectlombok</groupId>
                           <artifactId>lombok</artifactId>
                       </exclude>
                   </excludes>
               </configuration>
           </plugin>
       </plugins>
   </build>
</project>
```
  
We're using Project Lombok to reduce a lot of the JavaBeans code. Feel free to explore more in https://projectlombok.org

Manually add `<skipTests>true</skipTests>` property, since we're not going to be focusing on tests here, as well as Apache Commons Lang dependency.

```xml
<properties>
    ...
    <skipTests>true</skipTests>
</properties>
```

```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    ...
</dependencies>
```
<br/>

### 2.2. Create Domain Classes
Domain classes represent MongoDB documents that will be stored in different MongoDB collections. Rich mapping support is provided by the `MappingMongoConverter`. It has a rich metadata model that provides a full feature set to map domain objects to MongoDB documents. The mapping metadata model is populated by using annotations on your domain objects. However, the infrastructure is not limited to using annotations as the only source of metadata information.<sup>[3]</sup>

`Author.java`
```java
package com.everymundo.demo.domain;
 
import java.time.LocalDate;
 
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
 
import lombok.Data;
 
@Document("authors")
@Data
public class Author {
 
   @Id
   private String id;
   private String firstName;
   private String middleName;
   private String lastName;
   private LocalDate birthDate;
  
}
```

`Book.java`
```java
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
```

We're not going to be using the domain classes directly in the Aggregation example but the service that populates the DB needs them and they will give you a good idea of the information we'll be querying.

### 2.3. Create Data Transfer Classes
The Book Search feature will need several data transfer classes to receive the filters in the request and return the results in the response.

`LibraryFilter.java`
```java
package com.everymundo.demo.model.filter;
 
import lombok.Data;
 
@Data
public class LibraryFilter {
 
   private BookFilter book;
   private AuthorFilter author;
  
}
```

`AuthorFilter.java`
```java
package com.everymundo.demo.model.filter;
 
import org.apache.commons.lang3.StringUtils;
 
import lombok.Data;
 
@Data
public class AuthorFilter {
 
   private String firstName;
   private String middleName;
   private String lastName;
 
}
```

`BookFilter.java`
```java
package com.everymundo.demo.model.filter;
 
import org.apache.commons.lang3.StringUtils;
 
import lombok.Data;
 
@Data
public class BookFilter {
 
   private String name;
   private Integer year;
 
}
```

`AuthorData.java`
```java
package com.everymundo.demo.model;
 
import java.time.LocalDate;
 
import lombok.Data;
 
@Data
public class AuthorData {
 
   private String firstName;
   private String middleName;
   private String lastName;
   private LocalDate birthDate;
 
}
```

`BookData.java`
```java
package com.everymundo.demo.model;
 
import lombok.Data;
 
@Data
public class BookData {
 
   private String name;
   private int year;
   private AuthorData author;
  
}
```
<br/>

### 2.4. Create Functional Endpoint
Spring WebFlux includes WebFlux.fn, a lightweight functional programming model in which functions are used to route and handle requests and contracts are designed for immutability. It is an alternative to the annotation-based programming model but otherwise runs on the same Reactive Core foundation.

In WebFlux.fn, an HTTP request is handled with a `HandlerFunction`: a function that takes `ServerRequest` and returns a delayed `ServerResponse` (i.e. `Mono<ServerResponse>`). Both the request and the response object have immutable contracts that offer JDK 8-friendly access to the HTTP request and response. `HandlerFunction` is the equivalent of the body of a `@RequestMapping` method in the annotation-based programming model.

Incoming requests are routed to a handler function with a `RouterFunction`: a function that takes `ServerRequest` and returns a delayed `HandlerFunction` (i.e. `Mono<HandlerFunction>`). When the router function matches, a handler function is returned; otherwise an empty `Mono`. `RouterFunction` is the equivalent of a `@RequestMapping` annotation, but with the major difference that router functions provide not just data, but also behavior.<sup>[4]</sup>

The application exposes a single POST Book Search endpoint.

`RouteConfiguration.java`
```java
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
```

`LibraryHandler.java`
```java
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
```
<br/>

## References
1. Spring Data (https://spring.io/projects/spring-data)
2. Spring Data MongoDB (https://spring.io/projects/spring-data-mongodb)
3. Spring Data MongoDB - Mapping (https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mapping-chapter)
4. Spring WebFlux - Functional Endpoints (https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-fn)
