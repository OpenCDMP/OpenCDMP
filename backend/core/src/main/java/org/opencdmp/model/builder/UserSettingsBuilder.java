package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserSettingsEntity;
import org.opencdmp.model.UserSettings;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserSettingsBuilder extends BaseBuilder<UserSettings, UserSettingsEntity> {

	private final TenantScope tenantScope;

	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public UserSettingsBuilder(ConventionService conventionService, TenantScope tenantScope) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(UserSettingsBuilder.class)));
		this.tenantScope = tenantScope;
	}

	public UserSettingsBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<UserSettings> build(FieldSet fields, List<UserSettingsEntity> data) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

		List<UserSettings> models = new ArrayList<>();

		for (UserSettingsEntity d : data) {
			UserSettings m = new UserSettings();
			if (fields.hasField(this.asIndexer(UserSettings._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(UserSettings._value))) m.setValue(d.getValue());
			if (fields.hasField(this.asIndexer(UserSettings._key))) m.setKey(d.getKey());
			if (fields.hasField(this.asIndexer(UserSettings._type))) m.setType(d.getType());
			if (fields.hasField(this.asIndexer(UserSettings._entityId))) m.setEntityId(d.getEntityId());
			if (fields.hasField(this.asIndexer(UserSettings._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(UserSettings._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(UserSettings._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
			if (fields.hasField(this.asIndexer(UserSettings._hash))) m.setHash(this.hashValue(Instant.ofEpochMilli(d.getUpdatedAt().toEpochMilli())));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

}
