package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ShortToPlanStatusConverter implements Converter<Integer, PlanStatus> {
	@Override
	public PlanStatus convert(Integer source) {
		return PlanStatus.of(source.shortValue());
	}
}

