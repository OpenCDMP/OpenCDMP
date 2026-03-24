package org.opencdmp.model.builder.commonmodels.dto.planblueprint;

import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.models.planblueprint.SystemFieldModel;
import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.planblueprint.SystemField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.SystemFieldCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SystemFieldCommonModelBuilder extends FieldCommonModelBuilder<SystemField, SystemFieldModel> {

	@Autowired
	public SystemFieldCommonModelBuilder(
			ConventionService conventionService) {
		super(conventionService);
	}

	protected SystemField getInstance() {
		return new SystemField();
	}

	protected SystemField buildChild(SystemFieldModel data, SystemField model) {
		switch (data.getSystemFieldType()){
			case AccessRights -> model.setSystemFieldType(PlanBlueprintSystemFieldType.AccessRights);
			case Contact -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Contact);
			case Description -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Description);
			case Language -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Language);
			case Title -> model.setSystemFieldType(PlanBlueprintSystemFieldType.Title);
			case User -> model.setSystemFieldType(PlanBlueprintSystemFieldType.User);
			default -> throw new MyApplicationException("unrecognized type " + data.getSystemFieldType());
		}
		return model;
	}
}
