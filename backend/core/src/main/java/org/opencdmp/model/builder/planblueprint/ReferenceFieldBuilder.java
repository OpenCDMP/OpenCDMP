package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.types.planblueprint.ReferenceTypeFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.planblueprint.ReferenceTypeField;
import org.opencdmp.query.ReferenceTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceFieldBuilder extends FieldBuilder<ReferenceTypeField, ReferenceTypeFieldEntity> {
	private final QueryFactory queryFactory;

	private final BuilderFactory builderFactory;

	@Autowired
	public ReferenceFieldBuilder(
			ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceFieldBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	protected ReferenceTypeField getInstance() {
		return new ReferenceTypeField();
	}

	protected ReferenceTypeField buildChild(FieldSet fields, ReferenceTypeFieldEntity data, ReferenceTypeField model) {
		FieldSet referenceTypeFields = fields.extractPrefixed(this.asPrefix(ReferenceTypeField._referenceType));

		if (data.getReferenceTypeId() != null && !referenceTypeFields.isEmpty() ) model.setReferenceType(this.builderFactory.builder(ReferenceTypeBuilder.class).build(referenceTypeFields, this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(data.getReferenceTypeId()).first())); //TODO: Optimize
		if (fields.hasField(this.asIndexer(ReferenceTypeField._multipleSelect))) model.setMultipleSelect(data.getMultipleSelect());
		return model;
	}
}
