package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

@ReadingConverter
public class ShortToDescriptionStatusConverter implements Converter<Integer, DescriptionStatus> {
	@Override
	public DescriptionStatus convert(Integer source) {
		return DescriptionStatus.of(source.shortValue());
	}
}
