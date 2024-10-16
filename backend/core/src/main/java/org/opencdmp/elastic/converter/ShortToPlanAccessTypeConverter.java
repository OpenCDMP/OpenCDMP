package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanAccessType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ShortToPlanAccessTypeConverter implements Converter<Integer, PlanAccessType> {
	@Override
	public PlanAccessType convert(Integer source) {
		return PlanAccessType.of(source.shortValue());
	}
}
