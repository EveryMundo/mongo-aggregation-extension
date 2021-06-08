package com.everymundo.demo.mongodb.aggregation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

public class ExtendedAggregation {
	
	private ExtendedAggregation() {
	}
	
	/**
	 * Creates a new {@link LookupPipelineOperation}.
	 *
	 * @param from must not be {@literal null}.
	 * @param let must not be empty.
	 * @param as must not be {@literal null}.
	 * @param pipeline must not be empty.
	 * @return new instances of {@link LookupPipelineOperation}.
	 */
	public static LookupPipelineOperation lookup(String from, Map<String, String> let, String as, AggregationOperation...pipeline) {
		return lookup(from, let, as, Arrays.asList(pipeline));
	}
	
	/**
	 * Creates a new {@link LookupPipelineOperation}.
	 *
	 * @param from must not be {@literal null}.
	 * @param let must not be empty.
	 * @param as must not be {@literal null}.
	 * @param pipeline must not be empty.
	 * @return new instances of {@link LookupPipelineOperation}.
	 */
	public static LookupPipelineOperation lookup(String from, Map<String, String> let, String as, List<AggregationOperation> pipeline) {
		return new LookupPipelineOperation(from, let, pipeline, as);
	}
	
	/**
	 * Creates a new {@link MatchExprOperation} using the given expr.
	 *
	 * @param expr must not be {@literal null}.
	 * @return new instance of {@link MatchExprOperation}.
	 */
	public static MatchExprOperation matchExpr(AggregationExpression expr) {
		return new MatchExprOperation(expr);
	}

}
