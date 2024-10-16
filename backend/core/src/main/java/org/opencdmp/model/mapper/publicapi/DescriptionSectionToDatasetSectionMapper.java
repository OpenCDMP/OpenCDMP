package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.model.publicapi.datasetwizard.Section;
import org.springframework.stereotype.Component;

@Component
public class DescriptionSectionToDatasetSectionMapper {

    private final DescriptionFieldSetToDatasetFieldSetMapper descriptionFieldSetToDatasetFieldSetMapper;

    public DescriptionSectionToDatasetSectionMapper(DescriptionFieldSetToDatasetFieldSetMapper descriptionFieldSetToDatasetFieldSetMapper) {
        this.descriptionFieldSetToDatasetFieldSetMapper = descriptionFieldSetToDatasetFieldSetMapper;
    }

    public Section toPublicModel(org.opencdmp.model.descriptiontemplate.Section section, int page) {
        Section model = new Section();
        model.setId(section.getId());
        model.setDescription(section.getDescription());
        model.setOrdinal(section.getOrdinal());
        model.setTitle(section.getTitle());
        model.setSections(section.getSections().stream().map(x-> this.toPublicModel(x, page)).toList());
        model.setPage("page_" + page);
        model.setCompositeFields(section.getFieldSets().stream().map(this.descriptionFieldSetToDatasetFieldSetMapper::toPublicModel).toList());
        return model;
    }
}
