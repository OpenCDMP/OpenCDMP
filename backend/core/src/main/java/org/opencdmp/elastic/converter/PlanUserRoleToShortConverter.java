package org.opencdmp.elastic.converter;

import org.opencdmp.commons.enums.PlanUserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PlanUserRoleToShortConverter implements Converter<PlanUserRole, Short> {
	@Override
	public Short convert(PlanUserRole source) {
		return source.getValue();
	}
}
