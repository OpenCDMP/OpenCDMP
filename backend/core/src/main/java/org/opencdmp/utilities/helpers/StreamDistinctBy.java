package org.opencdmp.utilities.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamDistinctBy {

	public static <T> Predicate<T> distinctByKey(
			Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> found = new ConcurrentHashMap<>();
		return t -> found.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
