package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PlanStatusToShortConverter implements Converter<PlanStatus, Short> {
	@Override
	public Short convert(PlanStatus source) {
		return source.getValue();
	}
}

