package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.NumberDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.NumberData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.descriptiontemplate,NumberDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NumberDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<NumberData, NumberDataModel> {
    @Autowired
    public NumberDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NumberDataCommonModelBuilder.class)));
    }

    protected NumberData getInstance() {
        return new NumberData();
    }

    @Override
    protected void buildChild(NumberDataModel d, NumberData m) {
        
    }
}
