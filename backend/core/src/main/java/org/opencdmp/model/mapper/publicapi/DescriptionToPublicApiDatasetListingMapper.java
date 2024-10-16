package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.model.description.Description;
import org.opencdmp.model.publicapi.datasetprofile.DatasetProfilePublicModel;
import org.opencdmp.model.publicapi.grant.GrantPublicOverviewModel;
import org.opencdmp.model.publicapi.listingmodels.DatasetPublicListingModel;
import org.opencdmp.model.publicapi.user.UserInfoPublicModel;
import org.opencdmp.service.publicapi.PublicApiProperties;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Component
public class DescriptionToPublicApiDatasetListingMapper {

    public DatasetPublicListingModel toPublicListingModel(Description description, PublicApiProperties.ReferenceTypeMapConfig config) {
        DatasetPublicListingModel model = new DatasetPublicListingModel();
        model.setId(description.getId().toString());
        model.setLabel(description.getLabel());
        model.setDescription(description.getDescription());
        model.setVersion(0);

        model.setDmp(description.getPlan().getLabel());
        model.setDmpId(description.getPlan().getId().toString());
        model.setUsers(List.of(UserInfoPublicModel.fromDescriptionCreator(description.getCreatedBy())));
        model.setProfile(DatasetProfilePublicModel.fromDataModel(description.getDescriptionTemplate()));
        model.setGrant(GrantPublicOverviewModel.fromDescriptionReference(description.getDescriptionReferences(), config));

        model.setCreatedAt(Date.from(description.getCreatedAt()));
        model.setModifiedAt(Date.from(description.getUpdatedAt()));
        model.setFinalizedAt(Date.from(description.getFinalizedAt()));

        return model;
    }

}
