package org.opencdmp.model.builder.commonmodels;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PlanUserRole;
import org.opencdmp.commonmodels.models.PlanUserModel;
import org.opencdmp.commonmodels.models.UserModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanUserCommonModelBuilder extends BaseCommonModelBuilder<PlanUserModel, PlanUserEntity> {

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
    protected List<CommonModelBuilderItemResponse<PlanUserModel, PlanUserEntity>> buildInternal(List<PlanUserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items ", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null)
            return new ArrayList<>();


        Map<UUID, UserModel> userItemsMap = this.collectUsers(data);

        List<CommonModelBuilderItemResponse<PlanUserModel, PlanUserEntity>> models = new ArrayList<>();
        for (PlanUserEntity d : data) {
            PlanUserModel m = new PlanUserModel();
            switch (d.getRole()){
                case Viewer -> m.setRole(PlanUserRole.Viewer);
                case Owner -> m.setRole(PlanUserRole.Owner);
                case DescriptionContributor -> m.setRole(PlanUserRole.DescriptionContributor);
                case Reviewer -> m.setRole(PlanUserRole.Reviewer);
                default -> throw new MyApplicationException("unrecognized type " + d.getRole().getValue());
            }
            if (userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, UserModel> collectUsers(List<PlanUserEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", UserModel.class.getSimpleName());

        Map<UUID, UserModel> itemMap;
        UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).authorize(this.authorize).ids(data.stream().map(PlanUserEntity::getUserId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, UserEntity::getId);
        return itemMap;
    }

}
