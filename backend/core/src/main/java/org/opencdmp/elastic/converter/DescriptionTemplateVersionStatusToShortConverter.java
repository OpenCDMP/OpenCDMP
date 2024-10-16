package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DescriptionTemplateVersionStatusToShortConverter implements Converter<DescriptionTemplateVersionStatus, Short> {
	@Override
	public Short convert(DescriptionTemplateVersionStatus source) {
		return source.getValue();
	}
}
