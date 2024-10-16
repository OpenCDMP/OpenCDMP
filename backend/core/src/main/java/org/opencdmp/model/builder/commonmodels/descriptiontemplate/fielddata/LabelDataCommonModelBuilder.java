package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelDataEntity;
import org.opencdmp.convention.ConventionService;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LabelDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<LabelDataModel, LabelDataEntity> {
    @Autowired
    public LabelDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LabelDataCommonModelBuilder.class)));
    }

    protected LabelDataModel getInstance() {
        return new LabelDataModel();
    }

    @Override
    protected void buildChild(LabelDataEntity d, LabelDataModel m) {
        
    }
}
