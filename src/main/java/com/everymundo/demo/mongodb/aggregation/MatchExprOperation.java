package com.everymundo.demo.mongodb.aggregation;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.util.Assert;

public class MatchExprOperation implements AggregationOperation {
	
	private final AggregationExpression expr;
	

	/**
	 * Creates a new {@link MatchExprOperation} for the given expr.
	 *
	 * @param expr must not be {@literal null}.
	 */
	public MatchExprOperation(AggregationExpression expr) {
		Assert.notNull(expr, "Expr must not be null!");
		
		this.expr = expr;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#toDocument(org.springframework.data.mongodb.core.aggregation.AggregationOperationContext)
	 */
	@Override
	public Document toDocument(AggregationOperationContext context) {
		Document exprObject = new Document();
		exprObject.append("$expr", expr.toDocument(context));

		return new Document(getOperator(), exprObject);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#getOperator()
	 */
	@Override
	public String getOperator() {
		return "$match";
	}
	
}
