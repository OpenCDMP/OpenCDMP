package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.descriptiontemplate.Definition;
import org.opencdmp.model.publicapi.datasetwizard.PagedDatasetProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DescriptionTemplateToPublicApiDatasetProfileMapper {

    private static Logger logger = LoggerFactory.getLogger(DescriptionTemplateToPublicApiDatasetProfileMapper.class);

    private final DescriptionPageToDatasetPageMapper descriptionPageToDatasetPageMapper;

    public DescriptionTemplateToPublicApiDatasetProfileMapper(DescriptionPageToDatasetPageMapper descriptionPageToDatasetPageMapper) {
        this.descriptionPageToDatasetPageMapper = descriptionPageToDatasetPageMapper;
    }

    public PagedDatasetProfile toPublicModel(DescriptionTemplate descriptionTemplate) {
        PagedDatasetProfile model = new PagedDatasetProfile();
        Definition definition = descriptionTemplate.getDefinition();
        model.setStatus(descriptionTemplate.getStatus().getValue());
        model.setPages(definition.getPages().stream().map(
		        descriptionPageToDatasetPageMapper::toPublicModel
        ).toList());
        return model;
    }
}
