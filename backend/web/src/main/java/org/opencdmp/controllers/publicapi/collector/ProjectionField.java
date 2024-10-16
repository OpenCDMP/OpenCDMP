package org.opencdmp.controllers.publicapi.collector;

import jakarta.persistence.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectionField {
	private String key;
	private List<ProjectionField> fields;
	private ProjectionField parent;

	public ProjectionField(List<String> field, ProjectionField parent) {
		this.parse(field);
		this.parent = parent;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<ProjectionField> getFields() {
		return fields;
	}

	public void setFields(List<ProjectionField> fields) {
		this.fields = fields;
	}

	private void parse(List<String> fields) {
		String[] fieldsArr = fields.get(0).split("\\.");
		this.key = fieldsArr[0];
		this.fields = fields.stream()
				.map(x -> x.replace(x.contains(".") ? this.key + "." : this.key, ""))
				.filter(x -> !x.isEmpty())
				.map(x -> x.split("\\."))
				.collect(Collectors.groupingBy(x -> x[0]))
				.values()
				.stream()
				.map(field -> new ProjectionField(field.stream().map(x -> String.join(".", x)).collect(Collectors.toList()), this))
				.collect(Collectors.toList());
	}

	public Map collect(Map map, List<Tuple> tuples) {
		if (fields != null && !fields.isEmpty()) {
			this.collect(map, this.fields, tuples);
		} else {
			map.put(key, this.createObject(tuples, this.generatePath()));
		}
		return map;
	}

	public Map collect(Map map, List<ProjectionField> projectionGroup, List<Tuple> tuples) {
		map.put(key, this.createObject(tuples, projectionGroup));
		return map;
	}

	private String generatePath() {
		List<String> list = new LinkedList<>();
		ProjectionField current = this;
		list.add(current.key);
		while (current.parent != null) {
			list.add(current.parent.key);
			current = current.parent;
		}
		Collections.reverse(list);
		return String.join(".", list);
	}

	private Object createObject(List<Tuple> tuples, String field) {
		List values = tuples.stream()
				.map(x -> x.get(field))
				.distinct()
				.collect(Collectors.toList());

		if (values.size() > 1) {
			return values.stream().map(x -> {
				Map map = new LinkedHashMap();
				map.put(field, x);
				return map;
			}).collect(Collectors.toList());
		} else if (values.size() == 1) {
			return values.get(0);
		} else return null;
	}

	private Object createObject(List<Tuple> tuples, List<ProjectionField> group) {
		Collection<Map> values = tuples.stream()
				.map(x -> getValuesFrom(tuples, x, group))
				.collect(Collectors.toMap(x -> x.get("id"), x -> x, (x, y) -> x)).values();
		return values;
	}

	private Map getValuesFrom(List<Tuple> tuples, Tuple tuple, List<ProjectionField> group) {
		Map map = new LinkedHashMap();
		group.forEach(x -> {
			if (x.fields != null && !x.fields.isEmpty())
				map.put(x.key, this.createObject(tuples, x.fields));
			else map.put(x.key, tuple.get(x.generatePath()));
		});
		return map;
	}
}
