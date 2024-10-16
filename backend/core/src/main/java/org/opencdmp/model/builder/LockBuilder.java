package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.LockEntity;
import org.opencdmp.model.Lock;
import org.opencdmp.model.user.User;
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
    private final TenantScope tenantScope;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public LockBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, TenantScope tenantScope, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(LockBuilder.class)));
        this.builderFactory = builderFactory;
	    this.tenantScope = tenantScope;
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

        List<Lock> models = new ArrayList<>();
        for (LockEntity d : data) {
            Lock m = new Lock();
            if (fields.hasField(this.asIndexer(Lock._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Lock._target))) m.setTarget(d.getTarget());
            if (fields.hasField(this.asIndexer(Lock._targetType))) m.setTargetType(d.getTargetType());
            if (fields.hasField(this.asIndexer(Lock._lockedAt))) m.setLockedAt(d.getLockedAt());
            if (fields.hasField(this.asIndexer(Lock._touchedAt))) m.setTouchedAt(d.getTouchedAt());
            if (fields.hasField(this.asIndexer(Lock._hash))) m.setHash(this.hashValue(d.getTouchedAt()));
            if (fields.hasField(this.asIndexer(Lock._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
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
}
