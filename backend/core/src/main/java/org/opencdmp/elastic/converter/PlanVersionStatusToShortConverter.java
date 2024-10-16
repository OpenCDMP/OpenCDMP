package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanVersionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PlanVersionStatusToShortConverter implements Converter<PlanVersionStatus, Short> {
	@Override
	public Short convert(PlanVersionStatus source) {
		return source.getValue();
	}
}
