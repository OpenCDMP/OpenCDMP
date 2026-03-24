package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.descriptiontemplate,LabelDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LabelDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<LabelData, LabelDataModel> {
    @Autowired
    public LabelDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LabelDataCommonModelBuilder.class)));
    }

    protected LabelData getInstance() {
        return new LabelData();
    }

    @Override
    protected void buildChild(LabelDataModel d, LabelData m) {
        
    }
}
