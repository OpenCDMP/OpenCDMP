package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.commons.types.descriptiontemplate.RuleEntity;
import org.opencdmp.model.publicapi.datasetwizard.DefaultValueEntity;
import org.opencdmp.model.publicapi.datasetwizard.Field;
import org.opencdmp.model.publicapi.datasetwizard.FieldDescriptionEntity;
import org.opencdmp.model.publicapi.datasetwizard.VisibilityEntity;
import org.springframework.stereotype.Component;

@Component
public class DescriptionFieldToDatasetFieldMapper {

    public Field toPublicModel(org.opencdmp.model.descriptiontemplate.Field field) {
        Field model = new Field();
        model.setId(field.getId());
        model.setOrdinal(field.getOrdinal());
        model.setData(field.getData());
        model.setDatatype(field.getData().getFieldType().name());
        DefaultValueEntity defaultValueEntity = new DefaultValueEntity();
        defaultValueEntity.setType(field.getData().getFieldType().name());
        defaultValueEntity.setValue(field.getDefaultValue() != null ? field.getDefaultValue().getTextValue(): null); //TODO
        model.setDefaultValue(defaultValueEntity);
        model.setExport(field.getIncludeInExport());
        model.setSemantics(field.getSemantics());
        model.setValidations(field.getValidations().stream().map(x -> (int) x.getValue()).toList());
        VisibilityEntity visibilityEntity = new VisibilityEntity();
        visibilityEntity.setRules(field.getVisibilityRules().stream().map(x -> {
            RuleEntity ruleEntity = new RuleEntity();
            ruleEntity.setDateValue(x.getDateValue());
            ruleEntity.setBooleanValue(x.getBooleanValue());
            ruleEntity.setTarget(x.getTarget());
            return ruleEntity;
        }).toList());
        model.setVisible(visibilityEntity);
        FieldDescriptionEntity fieldDescriptionEntity = new FieldDescriptionEntity();
        model.setViewStyle(fieldDescriptionEntity);
        return model;
    }
}
