package org.opencdmp.model.builder.planblueprint;

import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.planblueprint.ExtraField;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExtraFieldBuilder extends FieldBuilder<ExtraField, ExtraFieldEntity> {

	@Autowired
	public ExtraFieldBuilder(
			ConventionService conventionService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(ExtraFieldBuilder.class)));
	}

	protected ExtraField getInstance() {
		return new ExtraField();
	}

	protected ExtraField buildChild(FieldSet fields, ExtraFieldEntity data, ExtraField model) {
		if (fields.hasField(this.asIndexer(ExtraField._dataType))) model.setDataType(data.getType());
		return model;
	}
}
