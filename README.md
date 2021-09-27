# How to Extend Spring Data Reactive MongoDB Aggregation Features
<br/>

## 1. Overview
Spring Data’s mission is to provide a familiar and consistent Spring-based programming model for data access, while still retaining the special traits of the underlying data store. It makes it easy to use data access technologies, relational and non-relational databases, map-reduce frameworks, and cloud-based data services.<sup>[1]</sup>

The Spring Data MongoDB project provides integration with the MongoDB document database. Key functional areas of Spring Data MongoDB are a POJO centric model for interacting with a MongoDB DBCollection and easily writing a Repository style data access layer.<sup>[2]</sup>

Spring Data MongoDB offers a bunch of nice features, however it does not support all the operations available in MongoDB. This tutorial will provide a clean way to extend Spring Data MongoDB aggregation features. We're not including all the supported operations. In case you need others that are not part of the tutorial, you can always apply the same approach.
<br/>
<br/>

## 2. Getting Started
In this tutorial we're going to show you how to easily extend Spring Data Reactive MongoDB aggregation features. You can start from scratch and complete each step or you can skip the steps that are already familiar to you and download the complete code [here](https://github.com/EveryMundo/mongo-aggregation-extension).

We'll be using Spring Boot 2.4.6, Spring Data Reactive MongoDB, Java 11, Apache Maven, MongoDB 4.4 and Docker. You can use slightly different versions for this tutorial, but at least Java 8 and MongoDB 4.x are recommended. If you don't know all the technologies, or if you're interested in taking a deep dive into any of them, you can check out the following links:

* Java (https://docs.oracle.com/en/java/index.html)
* Spring Framework (https://spring.io/projects/spring-framework)
* Spring Boot (https://spring.io/projects/spring-boot)
* Spring Data Reactive MongoDB (https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive)
* Apache Maven (https://maven.apache.org)
* MongoDB (https://docs.mongodb.com/manual)
* Docker (https://docs.docker.com)

**Note:**<br/>
We recommend an OpenJDK distribution of Java for development as well as production environments since Oracle has changed the Licence Agreement (see here) starting April 16, 2019. AdoptOpenJDK (https://adoptopenjdk.net) offers free pre-built OpenJDK binaries for Windows, Linux and MacOS.

This tutorial will implement an HTTP POST Book Search endpoint for a Library Service.
<br/>
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

We're not going to be using the domain classes directly in the Aggregation example, but the service that populates the DB needs them and they will give you a good idea of the information we'll be querying.

### 2.3. Create Data Transfer Classes
The Book Search feature will need several data transfer classes to receive the filters in the request and return the results in the response. To avoid making this tutorial too boring we're not going to add the code for those classes here. You can check `LibraryFilter`, `BookFilter`, `AuthorFilter`, `BookData` and `AuthorData` [here](https://github.com/EveryMundo/mongo-aggregation-extension/tree/master/src/main/java/com/everymundo/demo/model).
<br/>
<br/>

### 2.4. Create Functional Endpoint
Spring WebFlux includes WebFlux.fn, a lightweight functional programming model in which functions are used to route and handle requests, and contracts are designed for immutability. It is an alternative to the annotation-based programming model, but otherwise runs on the same Reactive Core foundation.

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

### 2.5. Create the Service
`LibraryService` implements `searchBooks` method, responsible for processing the filters, querying the database and returning the results. The `searchBooks` method uses `ReactiveMongoTemplate`, a Spring Data MongoDB class that simplifies the use of Reactive MongoDB usage and helps to avoid common errors. It executes core MongoDB workflow, leaving application code to provide `Document` and extract results. Additionally, several other Spring Data MongoDB classes are used to build a complex aggregation.

`LibraryService.java`
```java
package com.everymundo.demo.service;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
import com.everymundo.demo.model.BookData;
import com.everymundo.demo.model.filter.LibraryFilter;
import com.everymundo.demo.mongodb.aggregation.ExtendedAggregation;
import com.everymundo.demo.mongodb.aggregation.RegexOperators;
 
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
 
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
 
@Service
@RequiredArgsConstructor
public class LibraryService {
 
   private final ReactiveMongoTemplate mongoTemplate;
 
 
   public Flux<BookData> searchBooks(LibraryFilter filter) {
       return Flux.defer(() -> {
               List<AggregationOperation> stages = new ArrayList<>();
 
               if (filter.hasBookFilters()) {
                   Criteria bookCriteria = new Criteria();
 
                   // Book Name
                   if (StringUtils.isNotBlank(filter.getBook().getName())) {
                       bookCriteria.and("name").regex(".*" + filter.getBook().getName() + ".*", "i");
                   }
                   // Book Year
                   if (filter.getBook().getYear() != null) {
                       bookCriteria.and("year").is(filter.getBook().getYear());
                   }
 
                   // Match for Book Filters
                   stages.add(Aggregation.match(bookCriteria));
               }
 
               List<AggregationOperation> authorPipeline = new ArrayList<>();
              
               if (filter.hasAuthorFilter()) {
                   List<AggregationExpression> authorMatches = new ArrayList<>();
                  
                   // Compare book.authorId with author.id
                   authorMatches.add(ComparisonOperators.valueOf("$_id").equalTo(ConvertOperators.valueOf("$$authorId").convertToObjectId()));
 
                   // Author First Name
                   if (StringUtils.isNotBlank(filter.getAuthor().getFirstName())) {
                       authorMatches.add(RegexOperators.valueOf("$firstName").match(".*" + filter.getAuthor().getFirstName() + ".*", "i"));
                   }
                   // Author Middle Name
                   if (StringUtils.isNotBlank(filter.getAuthor().getMiddleName())) {
                       authorMatches.add(RegexOperators.valueOf("$middleName").match(".*" + filter.getAuthor().getMiddleName() + ".*", "i"));
                   }
                   // Author Last Name
                   if (StringUtils.isNotBlank(filter.getAuthor().getLastName())) {
                       authorMatches.add(RegexOperators.valueOf("$lastName").match(".*" + filter.getAuthor().getLastName() + ".*", "i"));
                   }
 
                   // Match to join and filter author
                   authorPipeline.add(ExtendedAggregation.matchExpr(BooleanOperators.And.and(authorMatches.toArray())));
               } else {
                   // Match to join author
                   authorPipeline.add(ExtendedAggregation.matchExpr(ComparisonOperators.valueOf("$_id").equalTo(ConvertOperators.valueOf("$$authorId").convertToObjectId())));
               }
 
               // Lookup to join author
               stages.add(ExtendedAggregation.lookup("authors", Map.of("authorId", "$authorId"), "author", authorPipeline));
 
               // Unwind author
               stages.add(Aggregation.unwind("$author", false));
 
               return Mono.just(stages);
           })
           .flatMap(stages -> this.mongoTemplate.aggregate(Aggregation.newAggregation(stages), "books", BookData.class));
   }
  
}
```

## 3. The Solution
Aggregation in MongoDB was built to process data and return computed results. Data is processed in stages and the output of one stage is provided as input to the next stage. This ability to apply transformations and do computations on data in stages makes aggregation a very powerful tool for analytics.

Spring Data MongoDB provides an abstraction for native aggregation queries using the three classes: `Aggregation`, which wraps an aggregation query; `AggregationOperation`, which wraps individual pipeline stages; and `AggregationResults`, which is the container of the result produced by aggregation.<sup>[5]</sup>

Spring Data MongoDB offers a comprehensive solution for MongoDB access and particularly, aggregations, but not all of the features are available. Trying to build a very complex search using the included aggregation features, we realized that some things were missing. In this example we're trying to use some aggregation advanced features, like the use of `pipeline` in a `$lookup` operation, or the ability to use a `$expr` inside a `$match`.

Target MongoDB Aggregation
```javascript
db.books.aggregate([
   {
       $match: {
           name: { $regex: ".*the.*", $options: "i" }
       }
   },
   {
       $lookup: {
           from: "authors",
           let: { authorId: "$authorId" },
           pipeline: [
               {
                   $match: {
                       $expr: {
                           $and: [
                               { $eq: ["$_id", { $toObjectId: "$$authorId" }] },
                               { $regexMatch: { input: "$firstName", regex: ".*John.*", options: "i" }}
                           ]
                       }
                   }
               }
           ],
           as: "author"
       }
   },
   {
       $unwind: {
           path: "$author",
           preserveNullAndEmptyArrays: false
       }
   }
])
```

There are a couple of ways to build the previous aggregation, including building a JSON representing the query. Here, we're focusing on building a solution that integrates with the aggregation features provided by Spring Data MongoDB.

To support `$match: { $expr: { … }}`, `MatchExprOperation` was created. It implements `AggregationOperation` like all of the provided operations, the implementation is just as simple as building a `Document` that represents the expected JSON output.

`MatchExprOperation.java`
```java
package com.everymundo.demo.mongodb.aggregation;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.util.Assert;

public class MatchExprOperation implements AggregationOperation {
	
	private final AggregationExpression expr;
	

	public MatchExprOperation(AggregationExpression expr) {
		Assert.notNull(expr, "Expr must not be null!");
		
		this.expr = expr;
	}
	
	@Override
	public Document toDocument(AggregationOperationContext context) {
		Document exprObject = new Document();
		exprObject.append("$expr", expr.toDocument(context));

		return new Document(getOperator(), exprObject);
	}

	@Override
	public String getOperator() {
		return "$match";
	}
	
}
```

You can see it in action here:
```java
// Match to join and filter author
authorPipeline.add(ExtendedAggregation.matchExpr(BooleanOperators.And.and(authorMatches.toArray())));
```

Getting the `$regexMatch` operators to work was a little trickier and we followed Spring's approach, similar to `BooleanOperators`, `ComparisonOperators` and `ConvertOperators` found in `org.springframework.data.mongodb.core.aggregation` package.

`RegexOperators.java`
```java
package com.everymundo.demo.mongodb.aggregation;
 
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.util.Assert;
 
public class RegexOperators {
 
   public static RegexOperatorFactory valueOf(String fieldReference) {
       return new RegexOperatorFactory(fieldReference);
   }
 
   public static class RegexOperatorFactory {
 
       private final String fieldReference;
 
       
       public RegexOperatorFactory(String fieldReference) {
           Assert.notNull(fieldReference, "FieldReference must not be null!");
           this.fieldReference = fieldReference;
       }
       
       public Match match(String regex) {
           return match(regex, null);
       }
        
       public Match match(String regex, String options) {
           Assert.notNull(regex, "Regex must not be null!");
           return Match.valueOf(fieldReference).match(regex, options);
       }
 
   }
 
   public static class Match implements AggregationExpression {
 
       private String input;
       private String regex;
       private String options;
 
 
       private Match(String input) {
           this(input, null);
       }
 
       private Match(String input, String options) {
           this.input = input;
           this.options = options;
       }
 
       public Match match(String regex) {
           return match(regex, null);
       }
 
       public Match match(String regex, String options) {
           this.regex = regex;
           this.options = options;
 
           return this;
       }
 
       @Override
       public Document toDocument(AggregationOperationContext context) {
           Document matchObject = new Document();
      
           matchObject.append("input", input);
           matchObject.append("regex", regex);
           if (StringUtils.isNotBlank(options)) {
               matchObject.append("options", options);
           }
          
           return new Document(getMongoMethod(), matchObject);
       }
 
       private String getMongoMethod() {
           return "$regexMatch";
       }
 
       public static Match valueOf(String fieldReference) {
           Assert.notNull(fieldReference, "FieldReference must not be null!");
           return new Match(fieldReference);
       }
      
   }
  
}
```

Here's an example:
```java
// Author First Name
if (StringUtils.isNotBlank(filter.getAuthor().getFirstName())) {
    authorMatches.add(RegexOperators.valueOf("$firstName").match(".*" + filter.getAuthor().getFirstName() + ".*", "i"));
}
```

`LookupPipelineOperation` represents a `$lookup` operation with support for the `pipeline` attribute, where you can add stages to filter, process or project-specific fields in the output.

`LookupPipelineOperation.java`
```java
package com.everymundo.demo.mongodb.aggregation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.ExposedFields;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.FieldsExposingAggregationOperation.InheritsFieldsAggregationOperation;
import org.springframework.util.Assert;

public class LookupPipelineOperation implements InheritsFieldsAggregationOperation {
	
	private final Field from;
	private final Map<String, String> let;
	private final List<AggregationOperation> pipeline;
	private final Field as;


	public LookupPipelineOperation(String from, Map<String, String> let, List<AggregationOperation> pipeline, String as) {
		Assert.notNull(from, "Field must not be null!");
		Assert.notEmpty(let, "Let must not be empty!");
		Assert.notEmpty(pipeline, "Pipeline must not be empty!");
		Assert.notNull(as, "As must not be null!");
		
		this.from = Fields.field(from);
		this.let = let;
		this.pipeline = pipeline;
		this.as = Fields.field(as);
	}
	
	@Override
	public ExposedFields getFields() {
		return ExposedFields.synthetic(Fields.from(as));
	}
	
	@Override
	public Document toDocument(AggregationOperationContext context) {
		Document lookupObject = new Document();
		
		Document letObject = new Document();
		let.entrySet().stream().forEach(entry -> letObject.append(entry.getKey(), entry.getValue()));

		lookupObject.append("from", from.getTarget());
		lookupObject.append("let", letObject);
		lookupObject.append("pipeline", pipeline.stream().map(stage -> stage.toPipelineStages(context).get(0)).collect(Collectors.toList()));
		lookupObject.append("as", as.getTarget());
		
		return new Document(getOperator(), lookupObject);
	}
	
	@Override
	public String getOperator() {
		return "$lookup";
	}

}
```

How to use `LookupPipelineOperation`:
```java
// Lookup to join author
stages.add(ExtendedAggregation.lookup("authors", Map.of("authorId", "$authorId"), "author", authorPipeline));
```

Finally, we created the `ExtendedAggregation` class, following the `Aggregation` class approach, to expose builder methods for both `MatchExprOperation` and `LookupPipelineOperation`.

`ExtendedAggregation`
```java
package com.everymundo.demo.mongodb.aggregation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

public class ExtendedAggregation {
	
	private ExtendedAggregation() {
	}
	
	public static LookupPipelineOperation lookup(String from, Map<String, String> let, String as, AggregationOperation...pipeline) {
		return lookup(from, let, as, Arrays.asList(pipeline));
	}
	
	public static LookupPipelineOperation lookup(String from, Map<String, String> let, String as, List<AggregationOperation> pipeline) {
		return new LookupPipelineOperation(from, let, pipeline, as);
	}
	
	public static MatchExprOperation matchExpr(AggregationExpression expr) {
		return new MatchExprOperation(expr);
	}

}
```

**Note:**<br/>
We kept the aggregation extension classes clean from any Lombok annotation in case anyone wanted to copy them as is, and use them.
<br/>
<br/>

## 4. Running the Application
To fully test the solution we recommend downloading the latest code from the Github repo. If you don't have MongoDB already installed, do so or skip the following if you already have MongoDB available. 

We strongly encourage the use of [Docker Desktop](https://www.docker.com/products/docker-desktop). After installing and starting it, go to the terminal and run the following command:
```
docker run --name=mongodb -p 27017:27017 -d mongo
```

After a couple of minutes you should have a running instance of MongoDB in port 27017.

Once everything is in place, go to the terminal and navigate to where the folder of the application is and execute the following:
```
mvn install && mvn spring-boot:run
```

Now the application is ready to receive traffic. Open another terminal (or REST client like Postman) and execute the following:
```
curl --location --request POST 'localhost:8080/books' \
--header 'Content-Type: application/json' \
--data-raw '{
    "book": {
        "name": "the"
    },
    "author": {
        "firstName": "john",
        "lastName": "kien"
    }
}'
```

Expect this response:
```json
[
    {
        "name": "The Hobbit",
        "year": 1937,
        "author": {
            "firstName": "John",
            "middleName": "Ronald Reuel",
            "lastName": "Tolkien",
            "birthDate": "1892-01-03"
        }
    },
    {
        "name": "The Lord of the Rings: The Two Towers",
        "year": 1954,
        "author": {
            "firstName": "John",
            "middleName": "Ronald Reuel",
            "lastName": "Tolkien",
            "birthDate": "1892-01-03"
        }
    },
    {
        "name": "The Lord of the Rings: The Fellowship of the Ring",
        "year": 1954,
        "author": {
            "firstName": "John",
            "middleName": "Ronald Reuel",
            "lastName": "Tolkien",
            "birthDate": "1892-01-03"
        }
    },
    {
        "name": "The Lord of the Rings: The Return of the King",
        "year": 1955,
        "author": {
            "firstName": "John",
            "middleName": "Ronald Reuel",
            "lastName": "Tolkien",
            "birthDate": "1892-01-03"
        }
    }
]
```

## Conclusion
Spring Data MongoDB is an awesome project that makes the life of the developers working with MongoDB much easier. It's very extensible and you can continue to expand its capabilities. However, not everything is full of happiness. We encountered some setbacks during the process of building the solution, mostly related to classes inside `org.springframework.data.mongodb.core.aggregation` package that are not accessible from outside. These include: `AbstractAggregationExpression`, a support class that implements several methods to manipulate the contents of an expression; or the class `ExposedField`, which represents an exposed field needed to mark a field available to be used in the next stage of the aggregation pipeline.

We still think Spring Data MongoDB is the best way to go if you're using Java and MongoDB. No matter the problems we found, Spring provides solid and easy-to-use ways to access and manipulate data in MongoDB.
<br/>
<br/>

## References
1. Spring Data (https://spring.io/projects/spring-data)
2. Spring Data MongoDB (https://spring.io/projects/spring-data-mongodb)
3. Spring Data MongoDB - Mapping (https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mapping-chapter)
4. Spring WebFlux - Functional Endpoints (https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-fn)
5. Spring Data MongoDB: Projections and Aggregations (https://www.baeldung.com/spring-data-mongodb-projections-aggregations)
