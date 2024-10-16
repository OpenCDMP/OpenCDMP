package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.IsActive;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class IsActiveToShortConverter implements Converter<IsActive, Short> {
	@Override
	public Short convert(IsActive source) {
		return source.getValue();
	}
}
