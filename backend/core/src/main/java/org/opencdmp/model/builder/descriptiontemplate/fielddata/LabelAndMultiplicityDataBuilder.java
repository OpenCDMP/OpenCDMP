package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelAndMultiplicityDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelAndMultiplicityData;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LabelAndMultiplicityDataBuilder extends BaseFieldDataBuilder<LabelAndMultiplicityData, LabelAndMultiplicityDataEntity> {

	@Autowired
	public LabelAndMultiplicityDataBuilder(ConventionService conventionService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(LabelAndMultiplicityDataBuilder.class)));
	}

	protected LabelAndMultiplicityData getInstance() {
		return new LabelAndMultiplicityData();
	}
	@Override
	protected void buildChild(FieldSet fields, LabelAndMultiplicityDataEntity d, LabelAndMultiplicityData m) {
		if (fields.hasField(this.asIndexer(LabelAndMultiplicityData._multipleSelect))) m.setMultipleSelect(d.getMultipleSelect());
	}
}
