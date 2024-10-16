package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.commons.types.descriptiontemplate.MultiplicityEntity;
import org.opencdmp.model.publicapi.datasetwizard.FieldSet;
import org.springframework.stereotype.Component;

@Component
public class DescriptionFieldSetToDatasetFieldSetMapper {

    private final DescriptionFieldToDatasetFieldMapper descriptionFieldToDatasetFieldMapper;

    public DescriptionFieldSetToDatasetFieldSetMapper(DescriptionFieldToDatasetFieldMapper descriptionFieldToDatasetFieldMapper) {
        this.descriptionFieldToDatasetFieldMapper = descriptionFieldToDatasetFieldMapper;
    }

    public FieldSet toPublicModel(org.opencdmp.model.descriptiontemplate.FieldSet fieldSet) {
        FieldSet model = new FieldSet();
        model.setId(fieldSet.getId());
        model.setDescription(fieldSet.getDescription());
        model.setOrdinal(fieldSet.getOrdinal());
        model.setTitle(fieldSet.getTitle());
        model.setAdditionalInformation(fieldSet.getAdditionalInformation());
        model.setExtendedDescription(fieldSet.getExtendedDescription());
        if (fieldSet.getMultiplicity() != null && fieldSet.getHasMultiplicity()) {
            MultiplicityEntity multiplicityEntity = new MultiplicityEntity();
            multiplicityEntity.setMin(fieldSet.getMultiplicity().getMin());
            multiplicityEntity.setMax(fieldSet.getMultiplicity().getMax());
            multiplicityEntity.setPlaceholder(fieldSet.getMultiplicity().getPlaceholder());
            multiplicityEntity.setTableView(fieldSet.getMultiplicity().getTableView());
            model.setMultiplicity(multiplicityEntity);
        }
        model.setFields(fieldSet.getFields().stream().map(this.descriptionFieldToDatasetFieldMapper::toPublicModel).toList());
        return model;
    }
}
