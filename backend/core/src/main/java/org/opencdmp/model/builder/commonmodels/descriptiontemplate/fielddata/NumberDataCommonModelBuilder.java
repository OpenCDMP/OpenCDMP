package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import gr.cite.tools.logging.LoggerService;

import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.NumberDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.NumberDataEntity;
import org.opencdmp.convention.ConventionService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NumberDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<NumberDataModel, NumberDataEntity> {
    @Autowired
    public NumberDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NumberDataCommonModelBuilder.class)));
    }

    protected NumberDataModel getInstance() {
        return new NumberDataModel();
    }

    @Override
    protected void buildChild(NumberDataEntity d, NumberDataModel m) {
        
    }
}
