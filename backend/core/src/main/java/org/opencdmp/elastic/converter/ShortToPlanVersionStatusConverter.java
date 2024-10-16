package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanVersionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ShortToPlanVersionStatusConverter implements Converter<Integer, PlanVersionStatus> {
	@Override
	public PlanVersionStatus convert(Integer source) {
		return PlanVersionStatus.of(source.shortValue());
	}
}
