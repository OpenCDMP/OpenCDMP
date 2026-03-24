package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelAndMultiplicityDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelAndMultiplicityData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.descriptiontemplate.LabelAndMultiplicityDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LabelAndMultiplicityDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<LabelAndMultiplicityData, LabelAndMultiplicityDataModel> {
    @Autowired
    public LabelAndMultiplicityDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LabelAndMultiplicityDataCommonModelBuilder.class)));
    }

    protected LabelAndMultiplicityData getInstance() {
        return new LabelAndMultiplicityData();
    }

    @Override
    protected void buildChild(LabelAndMultiplicityDataModel d, LabelAndMultiplicityData m) {
        m.setMultipleSelect(d.getMultipleSelect());
    }
}
