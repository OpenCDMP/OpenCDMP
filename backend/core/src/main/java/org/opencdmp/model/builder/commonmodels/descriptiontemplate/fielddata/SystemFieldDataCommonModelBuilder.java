package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.enums.SystemFieldType;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SystemFieldDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SystemFieldDataEntity;
import org.opencdmp.convention.ConventionService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SystemFieldDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<SystemFieldDataModel, SystemFieldDataEntity> {
    @Autowired
    public SystemFieldDataCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SystemFieldDataCommonModelBuilder.class)));
    }

    protected SystemFieldDataModel getInstance() {
        return new SystemFieldDataModel();
    }

    @Override
    protected void buildChild(SystemFieldDataEntity d, SystemFieldDataModel m) {
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
