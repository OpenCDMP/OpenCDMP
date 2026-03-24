package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.ReferenceTypeDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.dto.referencetype.ReferenceTypeCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.ReferenceTypeData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.descriptiontemplate.ReferenceTypeDataCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<ReferenceTypeData, ReferenceTypeDataModel> {

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

    protected ReferenceTypeData getInstance() {
        return new ReferenceTypeData();
    }

    @Override
    protected void buildChild(ReferenceTypeDataModel d, ReferenceTypeData m) {
        m.setMultipleSelect(d.getMultipleSelect());
        m.setReferenceType(this.builderFactory.builder(ReferenceTypeCommonModelBuilder.class).build(d.getReferenceType()));
        
    }
}
