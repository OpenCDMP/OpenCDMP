package org.opencdmp.model.builder.commonmodels.dto.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commonmodels.models.planblueprint.ReferenceTypeFieldModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.dto.referencetype.ReferenceTypeCommonModelBuilder;
import org.opencdmp.model.planblueprint.ReferenceTypeField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.planblueprint.ReferenceTypeFieldCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeFieldCommonModelBuilder extends FieldCommonModelBuilder<ReferenceTypeField, ReferenceTypeFieldModel> {
	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	@Autowired
	public ReferenceTypeFieldCommonModelBuilder(
			ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService);
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	protected ReferenceTypeField getInstance() {
		return new ReferenceTypeField();
	}

	protected ReferenceTypeField buildChild(ReferenceTypeFieldModel data, ReferenceTypeField model) {
		model.setReferenceType(this.builderFactory.builder(ReferenceTypeCommonModelBuilder.class).build(data.getReferenceType()));
		model.setMultipleSelect(data.getMultipleSelect());
		
		return model;
	}
}
