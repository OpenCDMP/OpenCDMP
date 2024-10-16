package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

@ReadingConverter
public class ShortToDescriptionTemplateVersionStatusConverter implements Converter<Integer, DescriptionTemplateVersionStatus> {
	@Override
	public DescriptionTemplateVersionStatus convert(Integer source) {
		return DescriptionTemplateVersionStatus.of(source.shortValue());
	}
}
