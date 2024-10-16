package org.opencdmp.model.builder.commonmodels.plan;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.plan.PlanBlueprintValueModel;
import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.types.plan.PlanBlueprintValueEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintValueCommonModelBuilder extends BaseCommonModelBuilder<PlanBlueprintValueModel, PlanBlueprintValueEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionEntity definition;

    @Autowired
    public PlanBlueprintValueCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintValueCommonModelBuilder.class)));
    }

    public PlanBlueprintValueCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanBlueprintValueCommonModelBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanBlueprintValueModel, PlanBlueprintValueEntity>> buildInternal(List<PlanBlueprintValueEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanBlueprintValueModel, PlanBlueprintValueEntity>> models = new ArrayList<>();
        for (PlanBlueprintValueEntity d : data) {
            FieldEntity fieldEntity = this.definition != null ? this.definition.getFieldById(d.getFieldId()).stream().findFirst().orElse(null) : null;

            if (fieldEntity != null && fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Extra)) {
                ExtraFieldEntity extraFieldEntity = (ExtraFieldEntity) fieldEntity;
                PlanBlueprintValueModel m = new PlanBlueprintValueModel();
                m.setFieldId(d.getFieldId());
                if (extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isDateType(extraFieldEntity.getType())){
                    m.setDateValue(d.getDateValue());
                } else if (extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isNumberType(extraFieldEntity.getType())){
                    m.setNumberValue(d.getNumberValue());
                } else {
                    m.setValue(d.getValue());
                }
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
