package org.opencdmp.model.builder.commonmodels.planblueprint;

import org.opencdmp.commonmodels.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.commonmodels.models.planblueprint.ExtraFieldModel;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.convention.ConventionService;
import gr.cite.tools.exception.MyApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExtraFieldCommonModelBuilder extends FieldCommonModelBuilder<ExtraFieldModel, ExtraFieldEntity> {

	@Autowired
	public ExtraFieldCommonModelBuilder(
			ConventionService conventionService) {
		super(conventionService);
	}

	protected ExtraFieldModel getInstance() {
		return new ExtraFieldModel();
	}

	protected ExtraFieldModel buildChild(ExtraFieldEntity data, ExtraFieldModel model) {
		switch (data.getType()){
			case Date -> model.setDataType(PlanBlueprintExtraFieldDataType.Date);
			case Number -> model.setDataType(PlanBlueprintExtraFieldDataType.Number);
			case RichTex -> model.setDataType(PlanBlueprintExtraFieldDataType.RichTex);
			case Text -> model.setDataType(PlanBlueprintExtraFieldDataType.Text);
			default -> throw new MyApplicationException("unrecognized type " + data.getType());
		}
		return model;
	}
}
