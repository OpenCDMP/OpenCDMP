package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.ReferenceTypeDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.ReferenceTypeData;
import org.opencdmp.query.ReferenceTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeDataBuilder extends BaseFieldDataBuilder<ReferenceTypeData, ReferenceTypeDataEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;

	@Autowired
	public ReferenceTypeDataBuilder(ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeDataBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	protected ReferenceTypeData getInstance() {
		return new ReferenceTypeData();
	}
	@Override
	protected void buildChild(FieldSet fields, ReferenceTypeDataEntity d, ReferenceTypeData m) {
		if (fields.hasField(this.asIndexer(ReferenceTypeData._multipleSelect))) m.setMultipleSelect(d.getMultipleSelect());

		FieldSet referenceTypeFields = fields.extractPrefixed(this.asPrefix(ReferenceTypeData._referenceType));
		if (d.getReferenceTypeId() != null && !referenceTypeFields.isEmpty() ) m.setReferenceType(this.builderFactory.builder(ReferenceTypeBuilder.class).build(referenceTypeFields, this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(d.getReferenceTypeId()).first())); //TODO: Optimize
	}
}
