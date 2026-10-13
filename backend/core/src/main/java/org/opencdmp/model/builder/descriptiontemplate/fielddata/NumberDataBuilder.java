package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.NumberDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.NumberData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NumberDataBuilder extends BaseFieldDataBuilder<NumberData, NumberDataEntity> {

	@Autowired
	public NumberDataBuilder(ConventionService conventionService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(NumberDataBuilder.class)));
	}

	protected NumberData getInstance() {
		return new NumberData();
	}

	@Override
	protected void buildChild(FieldSet fields, NumberDataEntity d, NumberData model) {
	}
}
