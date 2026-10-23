package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserPlanBlueprintEntity;
import org.opencdmp.model.UserPlanBlueprint;
import org.opencdmp.model.builder.planblueprint.PlanBlueprintBuilder;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.user.User;
import org.opencdmp.query.PlanBlueprintQuery;
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
public class UserPlanBlueprintBuilder extends BaseBuilder<UserPlanBlueprint, UserPlanBlueprintEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final TenantScopeFactory tenantScopeFactory;
    
    @Autowired
    public UserPlanBlueprintBuilder(
		    ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory, TenantScopeFactory tenantScopeFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserPlanBlueprintBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
	    this.tenantScopeFactory = tenantScopeFactory;
    }

    public UserPlanBlueprintBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UserPlanBlueprint> build(FieldSet fields, List<UserPlanBlueprintEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet planBlueprintFields = fields.extractPrefixed(this.asPrefix(UserPlanBlueprint._planBlueprint));
        Map<UUID, PlanBlueprint> planBlueprintMap = this.collectPlanBlueprints(planBlueprintFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(UserPlanBlueprint._user));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        List<UserPlanBlueprint> models = new ArrayList<>();
        for (UserPlanBlueprintEntity d : data) {
            UserPlanBlueprint m = new UserPlanBlueprint();
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._id)))  m.setId(d.getId());
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._role))) m.setRole(d.getRole());
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(UserPlanBlueprint._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));
            if (!planBlueprintFields.isEmpty() && planBlueprintMap != null && planBlueprintMap.containsKey(d.getPlanBlueprintId())) m.setPlanBlueprint(planBlueprintMap.get(d.getPlanBlueprintId()));
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<UserPlanBlueprintEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(UserPlanBlueprintEntity::getUserId).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(UserPlanBlueprintEntity::getUserId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, User::getId);
        }
        if (!fields.hasField(User._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, PlanBlueprint> collectPlanBlueprints(FieldSet fields, List<UserPlanBlueprintEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PlanBlueprint.class.getSimpleName());

        Map<UUID, PlanBlueprint> itemMap = null;
        if (!fields.hasOtherField(this.asIndexer(PlanBlueprint._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(UserPlanBlueprintEntity::getPlanBlueprintId).distinct().collect(Collectors.toList()),
                    x -> {
                        PlanBlueprint item = new PlanBlueprint();
                        item.setId(x);
                        return item;
                    },
                    x -> x.getId());
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PlanBlueprint._id);
            PlanBlueprintQuery q = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(data.stream().map(UserPlanBlueprintEntity::getPlanBlueprintId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBlueprintBuilder.class).asForeignKey(q, clone, PlanBlueprint::getId);
        }
        if (!fields.hasField(PlanBlueprint._id)) {
            itemMap.values().stream().filter(x -> x != null).map(x -> {
                x.setId(null);
                return x;
            }).collect(Collectors.toList());
        }

        return itemMap;
    }
}
