package com.everymundo.demo.mongodb.aggregation;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.util.Assert;

/**
 * Gateway to regex aggregation operations.
 */
public class RegexOperators {

    /**
	 * Take the array referenced by given {@literal fieldReference}.
	 *
	 * @param fieldReference must not be {@literal null}.
	 * @return new instance of {@link RegexOperatorFactory}.
	 */
	public static RegexOperatorFactory valueOf(String fieldReference) {
		return new RegexOperatorFactory(fieldReference);
	}

    public static class RegexOperatorFactory {

        private final String fieldReference;


        /**
		 * Creates new {@link RegexOperatorFactory} for given {@literal fieldReference}.
		 *
		 * @param fieldReference must not be {@literal null}.
		 */
		public RegexOperatorFactory(String fieldReference) {
			Assert.notNull(fieldReference, "FieldReference must not be null!");
			this.fieldReference = fieldReference;
		}

        /**
		 * Creates new {@link AggregationExpression} that takes the associated string representation and matches it against a regex.
		 *
		 * @param regex Regex expression
		 * @return new instance of {@link Match}.
		 */
		public Match match(String regex) {
            return match(regex, null);
		}

         /**
		 * Creates new {@link AggregationExpression} that takes the associated string representation and matches it against a regex
		 * with the specified options.
		 *
		 * @param regex Regex expression
		 * @param options Regex options.
		 * @return new instance of {@link Match}.
		 */
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

        /**
		 * @param regex Regex to match
		 * @return new instance of {@link Match}.
		 */
		public Match match(String regex) {
			return match(regex, null);
		}

        /**
		 * @param regex Regex to match
		 * @param options Regex options
		 * @return new instance of {@link Match}.
		 */
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

		/**
		 * Creates new {@link Match}.
		 *
		 * @param fieldReference must not be {@literal null}.
		 * @return new instance of {@link Match}.
		 */
		public static Match valueOf(String fieldReference) {
			Assert.notNull(fieldReference, "FieldReference must not be null!");
			return new Match(fieldReference);
		}
		
	}
    
}
