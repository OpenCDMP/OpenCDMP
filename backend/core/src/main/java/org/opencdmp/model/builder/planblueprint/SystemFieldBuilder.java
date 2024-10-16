package org.opencdmp.model.builder.planblueprint;

import org.opencdmp.commons.types.planblueprint.SystemFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.planblueprint.SystemField;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SystemFieldBuilder extends FieldBuilder<SystemField, SystemFieldEntity> {

	@Autowired
	public SystemFieldBuilder(
			ConventionService conventionService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(SystemFieldBuilder.class)));
	}

	protected SystemField getInstance() {
		return new SystemField();
	}

	protected SystemField buildChild(FieldSet fields, SystemFieldEntity data, SystemField model) {
		if (fields.hasField(this.asIndexer(SystemField._systemFieldType))) model.setSystemFieldType(data.getType());
		return model;
	}
}
