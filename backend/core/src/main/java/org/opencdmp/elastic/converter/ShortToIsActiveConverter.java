package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.IsActive;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

@ReadingConverter
public class ShortToIsActiveConverter implements Converter<Integer, IsActive> {
	@Override
	public IsActive convert(Integer source) {
		return IsActive.of(source.shortValue());
	}
}
