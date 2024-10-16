package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.publicapi.listingmodels.DataManagementPlanPublicListingModel;
import org.opencdmp.model.publicapi.researcher.ResearcherPublicModel;
import org.opencdmp.model.publicapi.user.UserInfoPublicModel;
import org.opencdmp.service.publicapi.PublicApiProperties;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Objects;

@Component
public class PlanToPublicApiPlanListingMapper {

    public DataManagementPlanPublicListingModel toPublicListingModel(Plan plan, PublicApiProperties.ReferenceTypeMapConfig config) {
        DataManagementPlanPublicListingModel model = new DataManagementPlanPublicListingModel();
        model.setId(plan.getId().toString());
        model.setLabel(plan.getLabel());
        model.setVersion(plan.getVersion());
        model.setGroupId(plan.getGroupId());

        model.setUsers(plan.getPlanUsers().stream().map(UserInfoPublicModel::fromDmpUser).toList());
        model.setResearchers(plan.getPlanReferences().stream().map(x-> ResearcherPublicModel.fromDmpReference(x, config)).filter(Objects::nonNull).toList());

        model.setCreatedAt(Date.from(plan.getCreatedAt()));
        model.setModifiedAt(Date.from(plan.getUpdatedAt()));
        model.setFinalizedAt(Date.from(plan.getFinalizedAt()));
        model.setPublishedAt(model.getFinalizedAt());

        return model;
    }

}
