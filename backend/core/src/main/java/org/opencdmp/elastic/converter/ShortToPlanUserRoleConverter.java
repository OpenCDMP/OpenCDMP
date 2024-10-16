package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanUserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ShortToPlanUserRoleConverter implements Converter<Integer, PlanUserRole> {
	@Override
	public PlanUserRole convert(Integer source) {
		return PlanUserRole.of(source.shortValue());
	}
}
