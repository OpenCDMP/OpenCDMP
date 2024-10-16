package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanAccessType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PlanAccessTypeToShortConverter implements Converter<PlanAccessType, Short> {
	@Override
	public Short convert(PlanAccessType source) {
		return source.getValue();
	}
}
