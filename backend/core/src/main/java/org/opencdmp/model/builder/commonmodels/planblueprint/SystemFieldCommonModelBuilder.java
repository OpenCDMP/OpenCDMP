package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.commonmodels.enums.PlanBlueprintSystemFieldType;
import org.opencdmp.commonmodels.models.planblueprint.SystemFieldModel;
import org.opencdmp.commons.types.planblueprint.SystemFieldEntity;
import org.opencdmp.convention.ConventionService;
import gr.cite.tools.exception.MyApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SystemFieldCommonModelBuilder extends FieldCommonModelBuilder<SystemFieldModel, SystemFieldEntity> {

	@Autowired
	public SystemFieldCommonModelBuilder(
			ConventionService conventionService) {
		super(conventionService);
	}

	protected SystemFieldModel getInstance() {
		return new SystemFieldModel();
	}

	protected SystemFieldModel buildChild(SystemFieldEntity data, SystemFieldModel model) {
		switch (data.getType()){
			case AccessRights -> model.setSystemFieldType(PlanBlueprintSystemFieldType.AccessRights);
			case Contact -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Contact);
			case Description -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Description);
			case Language -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Language);
			case Title -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Title);
			case User -> model.setSystemFieldType(PlanBlueprintSystemFieldType.User);
			default -> throw new MyApplicationException("unrecognized type " + data.getType());
		}
		return model;
	}
}
