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
