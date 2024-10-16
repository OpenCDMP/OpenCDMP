package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.ReferenceTypeDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.ReferenceTypeCommonModelBuilder;
import org.opencdmp.query.ReferenceTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<ReferenceTypeDataModel, ReferenceTypeDataEntity> {

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    @Autowired
    public ReferenceTypeDataCommonModelBuilder(
		    ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeDataCommonModelBuilder.class)));
	    this.queryFactory = queryFactory;
	    this.builderFactory = builderFactory;
    }

    protected ReferenceTypeDataModel getInstance() {
        return new ReferenceTypeDataModel();
    }

    @Override
    protected void buildChild(ReferenceTypeDataEntity d, ReferenceTypeDataModel m) {
        m.setMultipleSelect(d.getMultipleSelect());
        if (d.getReferenceTypeId() != null) m.setReferenceType(this.builderFactory.builder(ReferenceTypeCommonModelBuilder.class).build(this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(d.getReferenceTypeId()).first())); //TODO: Optimize
        
    }
}
