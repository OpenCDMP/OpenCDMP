package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.PublicPlanUser;
import org.opencdmp.model.PublicUser;
import org.opencdmp.model.user.User;
import org.opencdmp.query.PlanQuery;
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
public class PublicPlanUserBuilder extends BaseBuilder<PublicPlanUser, PlanUserEntity>{

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanUserBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanUserBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public PublicPlanUserBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanUser> build(FieldSet fields, List<PlanUserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(PublicPlanUser._user));
        Map<UUID, PublicUser> userItemsMap = this.collectUsers(userFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PublicPlanUser._plan));
        Map<UUID, PublicPlan> planItemsMap = this.collectPlans(planFields, data);

        List<PublicPlanUser> models = new ArrayList<>();
        for (PlanUserEntity d : data) {
            PublicPlanUser m = new PublicPlanUser();
            if (fields.hasField(this.asIndexer(PublicPlanUser._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicPlanUser._role))) m.setRole(d.getRole());
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId())) m.setPlan(planItemsMap.get(d.getPlanId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, PublicUser> collectUsers(FieldSet fields, List<PlanUserEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, PublicUser> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicUser._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanUserEntity::getUserId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicUser item = new PublicUser();
                        item.setId(x);
                        return item;
                    },
                    PublicUser::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicUser._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanUserEntity::getUserId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicUserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicUser::getId);
        }
        if (!fields.hasField(PublicUser._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, PublicPlan> collectPlans(FieldSet fields, List<PlanUserEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicPlan.class.getSimpleName());

        Map<UUID, PublicPlan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicPlan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanUserEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicPlan item = new PublicPlan();
                        item.setId(x);
                        return item;
                    },
                    PublicPlan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicPlan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanUserEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicPlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicPlan::getId);
        }
        if (!fields.hasField(PublicPlan._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

}
