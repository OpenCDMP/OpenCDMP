package org.opencdmp.model.builder.plan;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;
import org.opencdmp.commons.types.plan.PlanBlueprintValueEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.ExtraFieldEntity;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.plan.PlanBlueprintValue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("planblueprintvaluebuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintValueBuilder extends BaseBuilder<PlanBlueprintValue, PlanBlueprintValueEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private DefinitionEntity definition;

    @Autowired
    public PlanBlueprintValueBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintValueBuilder.class)));
    }

    public PlanBlueprintValueBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanBlueprintValueBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }

    @Override
    public List<PlanBlueprintValue> build(FieldSet planBlueprintValues, List<PlanBlueprintValueEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} PlanBlueprintValues", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(planBlueprintValues).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested PlanBlueprintValues", planBlueprintValues));
        if (planBlueprintValues == null || data == null || planBlueprintValues.isEmpty())
            return new ArrayList<>();


        List<PlanBlueprintValue> models = new ArrayList<>();
        for (PlanBlueprintValueEntity d : data) {
            FieldEntity fieldEntity = this.definition != null ? this.definition.getFieldById(d.getFieldId()).stream().findFirst().orElse(null) : null;

            if (fieldEntity != null && fieldEntity.getCategory().equals(PlanBlueprintFieldCategory.Extra)) {
                ExtraFieldEntity extraFieldEntity = (ExtraFieldEntity) fieldEntity;
                PlanBlueprintValue m = new PlanBlueprintValue();
                if (planBlueprintValues.hasField(this.asIndexer(PlanBlueprintValue._fieldId))) m.setFieldId(d.getFieldId());
                if (extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isDateType(extraFieldEntity.getType())){
                    if (planBlueprintValues.hasField(this.asIndexer(PlanBlueprintValue._dateValue))) m.setDateValue(d.getDateValue());
                } else if (extraFieldEntity != null && PlanBlueprintExtraFieldDataType.isNumberType(extraFieldEntity.getType())){
                    if (planBlueprintValues.hasField(this.asIndexer(PlanBlueprintValue._numberValue))) m.setNumberValue(d.getNumberValue());
                } else {
                    if (planBlueprintValues.hasField(this.asIndexer(PlanBlueprintValue._fieldValue))) m.setFieldValue(d.getValue());
                }

                models.add(m);
            }

        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
