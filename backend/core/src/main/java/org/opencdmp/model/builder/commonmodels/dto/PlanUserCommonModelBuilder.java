package org.opencdmp.model.builder.commonmodels.dto;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.PlanUserModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.PlanUserCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanUserCommonModelBuilder extends BaseCommonModelBuilder<PlanUser, PlanUserModel> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanUserCommonModelBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanUserCommonModelBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public PlanUserCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanUser, PlanUserModel>> buildInternal(List<PlanUserModel> data) throws MyApplicationException {
        this.logger.debug("building for {} items ", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null)
            return new ArrayList<>();

        List<CommonModelBuilderItemResponse<PlanUser, PlanUserModel>> models = new ArrayList<>();
        for (PlanUserModel d : data) {
            PlanUser m = new PlanUser();
            m.setSectionId(d.getSectionId());
            switch (d.getRole()){
                case Viewer -> m.setRole(PlanUserRole.Viewer);
                case Owner -> m.setRole(PlanUserRole.Owner);
                case DescriptionContributor -> m.setRole(PlanUserRole.DescriptionContributor);
                case Reviewer -> m.setRole(PlanUserRole.Reviewer);
                case DataSteward -> m.setRole(PlanUserRole.DataSteward);
                case DataPrivacyOfficer -> m.setRole(PlanUserRole.DataPrivacyOfficer);
                case EthicsReviewer -> m.setRole(PlanUserRole.EthicsReviewer);
                default -> throw new MyApplicationException("unrecognized type " + d.getRole().getValue());
            }
            m.setUser(this.builderFactory.builder(UserCommonModelBuilder.class).authorize(this.authorize).build(d.getUser()));
            m.setIsActive(IsActive.Active);
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
