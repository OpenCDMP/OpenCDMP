package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PlanBlueprintVersionStatusToShortConverter implements Converter<PlanBlueprintVersionStatus, Short> {
	@Override
	public Short convert(PlanBlueprintVersionStatus source) {
		return source.getValue();
	}
}
