package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.model.description.Description;
import org.opencdmp.model.publicapi.datasetprofile.DatasetProfilePublicModel;
import org.opencdmp.model.publicapi.datasetwizard.DataRepositoryPublicModel;
import org.opencdmp.model.publicapi.datasetwizard.ExternalDatasetPublicListingModel;
import org.opencdmp.model.publicapi.datasetwizard.RegistryPublicModel;
import org.opencdmp.model.publicapi.datasetwizard.ServicePublicModel;
import org.opencdmp.model.publicapi.listingmodels.DataManagementPlanPublicListingModel;
import org.opencdmp.model.publicapi.overviewmodels.DatasetPublicModel;
import org.opencdmp.service.publicapi.PublicApiProperties;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Objects;

@Component
public class DescriptionToPublicApiDatasetMapper {

    private final DescriptionTemplateToPublicApiDatasetProfileMapper descriptionTemplateToPublicApiDatasetProfileMapper;

    public DescriptionToPublicApiDatasetMapper(DescriptionTemplateToPublicApiDatasetProfileMapper descriptionTemplateToPublicApiDatasetProfileMapper) {
        this.descriptionTemplateToPublicApiDatasetProfileMapper = descriptionTemplateToPublicApiDatasetProfileMapper;
    }

    public DatasetPublicModel toPublicModel(Description description, DataManagementPlanPublicListingModel dmp, PublicApiProperties.ReferenceTypeMapConfig config) {
        DatasetPublicModel model = new DatasetPublicModel();
        model.setId(description.getId());
        model.setLabel(description.getLabel());
        model.setDescription(description.getDescription());
        model.setReference("");
        model.setUri("");
// TODO status       model.setStatus(description.getStatus());

        model.setDmp(dmp);
        model.setDatasetProfileDefinition(descriptionTemplateToPublicApiDatasetProfileMapper.toPublicModel(description.getDescriptionTemplate()));

        model.setProfile(DatasetProfilePublicModel.fromDataModel(description.getDescriptionTemplate()));
        if (description.getDescriptionReferences() != null) {
            model.setRegistries(description.getDescriptionReferences().stream().map(x-> RegistryPublicModel.fromDescriptionReference(x, config)).filter(Objects::nonNull).toList());
            model.setServices(description.getDescriptionReferences().stream().map(x-> ServicePublicModel.fromDescriptionReference(x, config)).filter(Objects::nonNull).toList());
            model.setDataRepositories(description.getDescriptionReferences().stream().map(x-> DataRepositoryPublicModel.fromDescriptionReference(x, config)).filter(Objects::nonNull).toList());
            model.setExternalDatasets(description.getDescriptionReferences().stream().map(x-> ExternalDatasetPublicListingModel.fromDescriptionReference(x, config)).filter(Objects::nonNull).toList());
        }

        model.setCreatedAt(Date.from(description.getCreatedAt()));
        model.setModifiedAt(Date.from(description.getUpdatedAt()));

        return model;
    }

}
