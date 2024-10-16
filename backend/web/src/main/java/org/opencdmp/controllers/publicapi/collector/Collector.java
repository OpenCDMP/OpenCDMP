package org.opencdmp.controllers.publicapi.collector;

import jakarta.persistence.Tuple;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Collector {

	public List<Map> buildFromTuple(List<Tuple> results, Map<Object, List<Tuple>> groupedResults, List<String> fields, String key) {
		return results.stream().map(tuple -> {
			Map<String, Object> parsedResult = new HashMap<>();
			tuple.getElements().forEach(tupleElement -> parsedResult.put(tupleElement.getAlias(), tuple.get(tupleElement.getAlias())));
			return parsedResult;
		}).collect(Collectors.toList());/*groupedResults.keySet().stream()
				.map(x -> buildOne(groupedResults.get(x), fields, key))
				.collect(Collectors.toList());*/
	}

	private Map buildOne(List<Tuple> tuples, List<String> fields, String key) {
		Map tupleValues = new LinkedHashMap();
		List<ProjectionField> projectionFields = fields.stream()
				.map(x -> x.split("\\."))
				.collect(Collectors.groupingBy(x -> x[0]))
				.values()
				.stream()
				.map(field -> new ProjectionField(field.stream().map(x -> String.join(".", x)).collect(Collectors.toList()), null))
				.collect(Collectors.toList());
		projectionFields.forEach(x -> x.collect(tupleValues, tuples));
		return tupleValues;
	}
}
