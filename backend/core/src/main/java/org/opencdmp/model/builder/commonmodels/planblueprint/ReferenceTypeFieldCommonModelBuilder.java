package org.opencdmp.model.builder.commonmodels.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commonmodels.models.planblueprint.ReferenceTypeFieldModel;
import org.opencdmp.commons.types.planblueprint.ReferenceTypeFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.ReferenceTypeCommonModelBuilder;
import org.opencdmp.query.ReferenceTypeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeFieldCommonModelBuilder extends FieldCommonModelBuilder<ReferenceTypeFieldModel, ReferenceTypeFieldEntity> {
	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	@Autowired
	public ReferenceTypeFieldCommonModelBuilder(
			ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService);
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	protected ReferenceTypeFieldModel getInstance() {
		return new ReferenceTypeFieldModel();
	}

	protected ReferenceTypeFieldModel buildChild(ReferenceTypeFieldEntity data, ReferenceTypeFieldModel model) {
		if (data.getReferenceTypeId() != null ) model.setReferenceType(this.builderFactory.builder(ReferenceTypeCommonModelBuilder.class).build(this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(data.getReferenceTypeId()).first())); //TODO: Optimize
		model.setMultipleSelect(data.getMultipleSelect());
		
		return model;
	}
}
