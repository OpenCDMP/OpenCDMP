package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DescriptionStatusToShortConverter implements Converter<DescriptionStatus, Short> {
	@Override
	public Short convert(DescriptionStatus source) {
		return source.getValue();
	}
}
