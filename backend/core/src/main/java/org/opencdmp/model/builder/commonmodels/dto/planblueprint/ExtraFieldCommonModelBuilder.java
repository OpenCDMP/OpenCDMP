package org.opencdmp.model.builder.commonmodels.dto.planblueprint;

import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commonmodels.models.planblueprint.ExtraFieldModel;
import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.planblueprint.ExtraField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.ExtraFieldCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExtraFieldCommonModelBuilder extends FieldCommonModelBuilder<ExtraField, ExtraFieldModel> {

	@Autowired
	public ExtraFieldCommonModelBuilder(
			ConventionService conventionService) {
		super(conventionService);
	}

	protected ExtraField getInstance() {
		return new ExtraField();
	}

	protected ExtraField buildChild(ExtraFieldModel data, ExtraField model) {
		switch (data.getDataType()){
			case Date -> model.setDataType(PlanBlueprintExtraFieldDataType.Date);
			case Number -> model.setDataType(PlanBlueprintExtraFieldDataType.Number);
			case RichTex -> model.setDataType(PlanBlueprintExtraFieldDataType.RichTex);
			case Text -> model.setDataType(PlanBlueprintExtraFieldDataType.Text);
			default -> throw new MyApplicationException("unrecognized type " + data.getDataType());
		}
		return model;
	}
}
