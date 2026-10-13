package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SystemFieldDataModel;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.fielddata.SystemFieldData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("dto.descriptiontemplate,SystemFieldDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SystemFieldDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<SystemFieldData, SystemFieldDataModel> {
    @Autowired
    public SystemFieldDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SystemFieldDataCommonModelBuilder.class)));
    }

    protected SystemFieldData getInstance() {
        return new SystemFieldData();
    }

    @Override
    protected void buildChild(SystemFieldDataModel d, SystemFieldData m) {
        switch (d.getSystemFieldType()){
            case PlanTitle -> m.setSystemFieldType(SystemFieldType.PlanTitle);
            case PlanDescription -> m.setSystemFieldType(SystemFieldType.PlanDescription);
            case PlanLanguage -> m.setSystemFieldType(SystemFieldType.PlanLanguage);
            case PlanContact -> m.setSystemFieldType(SystemFieldType.PlanContact);
            case PlanAccessRights -> m.setSystemFieldType(SystemFieldType.PlanAccessRights);
            case PlanUser -> m.setSystemFieldType(SystemFieldType.PlanUser);
            case DescriptionTitle -> m.setSystemFieldType(SystemFieldType.DescriptionTitle);
            case DescriptionDescription -> m.setSystemFieldType(SystemFieldType.DescriptionDescription);
            case DescriptionTag -> m.setSystemFieldType(SystemFieldType.DescriptionTag);
            case null -> m.setSystemFieldType(null);
            default -> throw new MyApplicationException("unrecognized type " + d.getSystemFieldType());
        }
    }
}
