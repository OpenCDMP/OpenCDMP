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
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.TenantUser;
import org.opencdmp.model.user.User;
import org.opencdmp.query.TenantQuery;
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
public class TenantUserBuilder extends BaseBuilder<TenantUser, TenantUserEntity> {

	private final BuilderFactory builderFactory;
	private final QueryFactory queryFactory;
	private final TenantScope tenantScope;

	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public TenantUserBuilder(
			ConventionService conventionService,
			BuilderFactory builderFactory,
			QueryFactory queryFactory, TenantScope tenantScope
	) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(TenantUserBuilder.class)));
		this.builderFactory = builderFactory;
		this.queryFactory = queryFactory;
		this.tenantScope = tenantScope;
	}

	public TenantUserBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<TenantUser> build(FieldSet fields, List<TenantUserEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(e -> e.size()).orElse(0), Optional.ofNullable(fields).map(e -> e.getFields()).map(e -> e.size()).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || fields.isEmpty()) return new ArrayList<>();

		FieldSet userFields = fields.extractPrefixed(this.asPrefix(TenantUser._user));
		Map<UUID, User> userMap = this.collectUsers(userFields, datas);

		FieldSet tenantFields = fields.extractPrefixed(this.asPrefix(TenantUser._tenant));
		Map<UUID, Tenant> tenantMap = this.collectTenants(tenantFields, datas);

		List<TenantUser> models = new ArrayList<>();

		for (TenantUserEntity d : datas) {
			TenantUser m = new TenantUser();
			if (fields.hasField(this.asIndexer(TenantUser._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(TenantUser._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (fields.hasField(this.asIndexer(TenantUser._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(TenantUser._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(TenantUser._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(TenantUser._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
			if (!userFields.isEmpty() && userMap != null && userMap.containsKey(d.getUserId())) m.setUser(userMap.get(d.getUserId()));
			if (!tenantFields.isEmpty() && tenantMap != null && tenantMap.containsKey(d.getTenantId())) m.setTenant(tenantMap.get(d.getTenantId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.ofNullable(models).map(e -> e.size()).orElse(0));
		return models;
	}

	private Map<UUID, User> collectUsers(FieldSet fields, List<TenantUserEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", User.class.getSimpleName());

		Map<UUID, User> itemMap = null;
		if (!fields.hasOtherField(this.asIndexer(User._id))) {
			itemMap = this.asEmpty(
					datas.stream().map(x -> x.getUserId()).distinct().collect(Collectors.toList()),
					x -> {
						User item = new User();
						item.setId(x);
						return item;
					},
					x -> x.getId());
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
			UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(datas.stream().map(x -> x.getUserId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, x -> x.getId());
		}
		if (!fields.hasField(User._id)) {
			itemMap.values().stream().filter(x -> x != null).map(x -> {
				x.setId(null);
				return x;
			}).collect(Collectors.toList());
		}

		return itemMap;
	}

	private Map<UUID, Tenant> collectTenants(FieldSet fields, List<TenantUserEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", Tenant.class.getSimpleName());

		Map<UUID, Tenant> itemMap = null;
		if (!fields.hasOtherField(this.asIndexer(Tenant._id))) {
			itemMap = this.asEmpty(
					datas.stream().map(x -> x.getTenantId()).distinct().collect(Collectors.toList()),
					x -> {
						Tenant item = new Tenant();
						item.setId(x);
						return item;
					},
					x -> x.getId());
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Tenant._id);
			TenantQuery q = this.queryFactory.query(TenantQuery.class).disableTracking().authorize(this.authorize).ids(datas.stream().map(x -> x.getTenantId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(TenantBuilder.class).authorize(this.authorize).asForeignKey(q, clone, x -> x.getId());
		}
		if (!fields.hasField(Tenant._id)) {
			itemMap.values().stream().filter(x -> x != null).map(x -> {
				x.setId(null);
				return x;
			}).collect(Collectors.toList());
		}

		return itemMap;
	}
}
