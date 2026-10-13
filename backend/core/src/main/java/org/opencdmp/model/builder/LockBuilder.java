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
import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.data.LockEntity;
import org.opencdmp.model.Lock;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.user.User;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.PlanBlueprintQuery;
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
public class LockBuilder extends BaseBuilder<Lock, LockEntity>{

    private final BuilderFactory builderFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public LockBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, TenantScopeFactory tenantScopeFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LockBuilder.class)));
        this.builderFactory = builderFactory;
	    this.tenantScopeFactory = tenantScopeFactory;
	    this.queryFactory = queryFactory;
    }

    public LockBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Lock> build(FieldSet fields, List<LockEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(Lock._lockedBy));
        Map<UUID, User> userMap = this.collectUsers(userFields, data);
        Map<UUID, String> targetLabels = fields.hasField(this.asIndexer(Lock._targetLabel)) ? this.collectTargetLabels(data) : null;

        List<Lock> models = new ArrayList<>();
        for (LockEntity d : data) {
            Lock m = new Lock();
            if (fields.hasField(this.asIndexer(Lock._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Lock._target))) m.setTarget(d.getTarget());
            if (fields.hasField(this.asIndexer(Lock._targetType))) m.setTargetType(d.getTargetType());
            if (fields.hasField(this.asIndexer(Lock._lockedAt))) m.setLockedAt(d.getLockedAt());
            if (fields.hasField(this.asIndexer(Lock._touchedAt))) m.setTouchedAt(d.getTouchedAt());
            if (fields.hasField(this.asIndexer(Lock._hash))) m.setHash(this.hashValue(d.getTouchedAt()));
            if (fields.hasField(this.asIndexer(Lock._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));
            if (targetLabels != null) m.setTargetLabel(targetLabels.get(d.getTarget()));
            if (!userFields.isEmpty() && userMap != null && userMap.containsKey(d.getLockedBy())) m.setLockedBy(userMap.get(d.getLockedBy()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<LockEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(LockEntity::getLockedBy).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(LockEntity::getLockedBy).distinct().collect(Collectors.toList()));
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

    private Map<UUID, String> collectTargetLabels(List<LockEntity> data) {
        Map<UUID, String> labels = new HashMap<>();
        List<UUID> planIds = this.targetsOf(data, LockTargetType.Plan);
        if (!planIds.isEmpty()) this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).ids(planIds).collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._label)).forEach(x -> labels.put(x.getId(), x.getLabel()));
        List<UUID> descriptionIds = this.targetsOf(data, LockTargetType.Description);
        if (!descriptionIds.isEmpty()) this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(this.authorize).ids(descriptionIds).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._label)).forEach(x -> labels.put(x.getId(), x.getLabel()));
        List<UUID> blueprintIds = this.targetsOf(data, LockTargetType.PlanBlueprint);
        if (!blueprintIds.isEmpty()) this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(this.authorize).ids(blueprintIds).collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._label)).forEach(x -> labels.put(x.getId(), x.getLabel()));
        List<UUID> templateIds = this.targetsOf(data, LockTargetType.DescriptionTemplate);
        if (!templateIds.isEmpty()) this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(templateIds).collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._label)).forEach(x -> labels.put(x.getId(), x.getLabel()));
        return labels;
    }

    private List<UUID> targetsOf(List<LockEntity> data, LockTargetType type) {
        return data.stream().filter(x -> x.getTargetType() == type).map(LockEntity::getTarget).distinct().collect(Collectors.toList());
    }
}
