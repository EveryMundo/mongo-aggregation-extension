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


	/**
	 * Creates a new {@link LookupPipelineOperation} for the given field and values.
	 *
	 * @param from must not be {@literal null}.
	 * @param let must not be empty.
	 * @param pipeline must not be empty.
	 * @param as must not be {@literal null}.
	 */
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
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.core.aggregation.FieldsExposingAggregationOperation#getFields()
	 */
	@Override
	public ExposedFields getFields() {
		return ExposedFields.synthetic(Fields.from(as));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#toDocument(org.springframework.data.mongodb.core.aggregation.AggregationOperationContext)
	 */
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
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#getOperator()
	 */
	@Override
	public String getOperator() {
		return "$lookup";
	}

}
