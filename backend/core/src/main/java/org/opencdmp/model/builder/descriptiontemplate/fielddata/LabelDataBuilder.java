package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelData;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LabelDataBuilder extends BaseFieldDataBuilder<LabelData, LabelDataEntity> {

	@Autowired
	public LabelDataBuilder(ConventionService conventionService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(LabelDataBuilder.class)));
	}

	protected LabelData getInstance() {
		return new LabelData();
	}

	@Override
	protected void buildChild(FieldSet fields, LabelDataEntity d, LabelData model) {
	}
}
