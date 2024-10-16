package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.model.descriptiontemplate.Page;
import org.opencdmp.model.publicapi.datasetwizard.DatasetProfilePage;
import org.springframework.stereotype.Component;

@Component
public class DescriptionPageToDatasetPageMapper {

    private final DescriptionSectionToDatasetSectionMapper descriptionSectionToDatasetSectionMapper;

    public DescriptionPageToDatasetPageMapper(DescriptionSectionToDatasetSectionMapper descriptionSectionToDatasetSectionMapper) {
        this.descriptionSectionToDatasetSectionMapper = descriptionSectionToDatasetSectionMapper;
    }

    public DatasetProfilePage toPublicModel(Page page) {
        DatasetProfilePage model = new DatasetProfilePage();
        model.setOrdinal(page.getOrdinal());
        model.setTitle(page.getTitle());
        if (page.getSections() != null) model.setSections(page.getSections().stream().map(x-> descriptionSectionToDatasetSectionMapper.toPublicModel(x, page.getOrdinal())).toList());
        return model;
    }
}
