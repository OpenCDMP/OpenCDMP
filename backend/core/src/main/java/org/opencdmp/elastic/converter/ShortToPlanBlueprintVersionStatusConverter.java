package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ShortToPlanBlueprintVersionStatusConverter implements Converter<Integer, PlanBlueprintVersionStatus> {
	@Override
	public PlanBlueprintVersionStatus convert(Integer source) {
		return PlanBlueprintVersionStatus.of(source.shortValue());
	}
}
