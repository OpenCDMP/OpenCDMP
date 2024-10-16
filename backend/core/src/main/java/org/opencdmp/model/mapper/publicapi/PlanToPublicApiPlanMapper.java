package org.opencdmp.model.mapper.publicapi;

import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.publicapi.associatedprofile.AssociatedProfilePublicModel;
import org.opencdmp.model.publicapi.doi.DoiPublicModel;
import org.opencdmp.model.publicapi.grant.GrantPublicOverviewModel;
import org.opencdmp.model.publicapi.listingmodels.DataManagementPlanPublicListingModel;
import org.opencdmp.model.publicapi.organisation.OrganizationPublicModel;
import org.opencdmp.model.publicapi.overviewmodels.DataManagementPlanPublicModel;
import org.opencdmp.model.publicapi.researcher.ResearcherPublicModel;
import org.opencdmp.model.publicapi.user.UserInfoPublicModel;
import org.opencdmp.service.publicapi.PublicApiProperties;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;
import java.util.Objects;

@Component
public class PlanToPublicApiPlanMapper {

    private final DescriptionToPublicApiDatasetMapper descriptionToPublicApiDatasetMapper;

    public PlanToPublicApiPlanMapper(DescriptionToPublicApiDatasetMapper descriptionToPublicApiDatasetMapper) {
        this.descriptionToPublicApiDatasetMapper = descriptionToPublicApiDatasetMapper;
    }

    public DataManagementPlanPublicModel toPublicModel(Plan plan, List<EntityDoiEntity> doiEntities, List<PlanDescriptionTemplate> descriptionTemplates, PublicApiProperties.ReferenceTypeMapConfig config) {
        DataManagementPlanPublicModel model = new DataManagementPlanPublicModel();
        model.setId(plan.getId().toString());
        model.setLabel(plan.getLabel());
        model.setDescription(plan.getDescription());
        model.setVersion(plan.getVersion());
        model.setGroupId(plan.getGroupId());
        if (plan.getBlueprint() != null) model.setProfile(plan.getBlueprint().getLabel());

        if (plan.getDescriptions() != null) {
            DataManagementPlanPublicListingModel publicListingModel = new DataManagementPlanPublicListingModel();
            publicListingModel.setId(model.getId());
            model.setDatasets(plan.getDescriptions().stream().map(x -> descriptionToPublicApiDatasetMapper.toPublicModel(x, publicListingModel, config)).toList());
        }

        model.setUsers(plan.getPlanUsers().stream().map(UserInfoPublicModel::fromDmpUser).toList());
        model.setResearchers(plan.getPlanReferences().stream().map(x-> ResearcherPublicModel.fromDmpReference(x, config)).filter(Objects::nonNull).toList());
        model.setGrant(GrantPublicOverviewModel.fromPlanReferences(plan.getPlanReferences(), config));
        model.setOrganisations(plan.getPlanReferences().stream().map(x-> OrganizationPublicModel.fromPlanReference(x, config)).filter(Objects::nonNull).toList());
        model.setDois(doiEntities.stream().map(DoiPublicModel::fromDataModel).toList());
        model.setAssociatedProfiles(descriptionTemplates.stream().map(x -> AssociatedProfilePublicModel.fromPlanDescriptionTemplate(x, plan.getBlueprint())).toList());

        model.setCreatedAt(Date.from(plan.getCreatedAt()));
        model.setModifiedAt(Date.from(plan.getUpdatedAt()));
        model.setFinalizedAt(Date.from(plan.getFinalizedAt()));

        return model;
    }
}

