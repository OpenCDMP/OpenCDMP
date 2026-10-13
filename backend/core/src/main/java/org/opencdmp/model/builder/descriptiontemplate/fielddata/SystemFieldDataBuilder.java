package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SystemFieldDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.SystemFieldData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SystemFieldDataBuilder extends BaseFieldDataBuilder<SystemFieldData, SystemFieldDataEntity> {
	private final BuilderFactory builderFactory;

	@Autowired
	public SystemFieldDataBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(SystemFieldDataBuilder.class)));
		this.builderFactory = builderFactory;
	}

	protected SystemFieldData getInstance() {
		return new SystemFieldData();
	}
	@Override
	protected void buildChild(FieldSet fields, SystemFieldDataEntity d, SystemFieldData m) {

		if (fields.hasField(this.asIndexer(SystemFieldData._systemFieldType))) m.setSystemFieldType(d.getSystemFieldType());
	}
}
