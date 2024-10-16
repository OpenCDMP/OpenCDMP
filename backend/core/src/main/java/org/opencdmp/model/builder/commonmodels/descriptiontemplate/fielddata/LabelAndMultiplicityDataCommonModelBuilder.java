package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelAndMultiplicityDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelAndMultiplicityDataEntity;
import org.opencdmp.convention.ConventionService;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LabelAndMultiplicityDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<LabelAndMultiplicityDataModel, LabelAndMultiplicityDataEntity> {
    @Autowired
    public LabelAndMultiplicityDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LabelAndMultiplicityDataCommonModelBuilder.class)));
    }

    protected LabelAndMultiplicityDataModel getInstance() {
        return new LabelAndMultiplicityDataModel();
    }

    @Override
    protected void buildChild(LabelAndMultiplicityDataEntity d, LabelAndMultiplicityDataModel m) {
        m.setMultipleSelect(d.getMultipleSelect());
    }
}
