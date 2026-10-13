package org.opencdmp.service.user.settings;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.UserSettingsEntity;
import org.opencdmp.model.UserSettings;
import org.opencdmp.model.builder.UserSettingsBuilder;
import org.opencdmp.model.persist.UserSettingsPersist;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserSettingsServiceImpl.class));
	private final TenantEntityManagerFactory tenantEntityManagerFactory;
	private final AuthorizationService authorizationService;
	private final BuilderFactory builderFactory;
	private final ConventionService conventionService;
	private final MessageSource messageSource;
	private final UserScopeFactory userScopeFactory;
	private final TenantScopeFactory tenantScopeFactory;


	@Autowired
	public UserSettingsServiceImpl(
			TenantEntityManagerFactory tenantEntityManagerFactory,
			AuthorizationService authorizationService,
			BuilderFactory builderFactory,
			ConventionService conventionService,
			MessageSource messageSource,
			UserScopeFactory userScopeFactory,
			TenantScopeFactory tenantScopeFactory) {
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
		this.tenantScopeFactory = tenantScopeFactory;
		this.authorizationService = authorizationService;
		this.builderFactory = builderFactory;
		this.conventionService = conventionService;
		this.messageSource = messageSource;
		this.userScopeFactory = userScopeFactory;
	}

	@Override
	public UserSettings persist(UserSettingsPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting data access request").And("model", model).And("fields", fields));

		boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		UserSettingsEntity data = null;
		UUID existingOwner = null;
		if (isUpdate) {
			data = this.tenantEntityManagerFactory.getInstance().find(UserSettingsEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), UserSettings.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			existingOwner = data.getEntityId();
			// The tenant filter also lets tenant-less rows through, so a client inside a tenant can be handed the
			// default-scope row. Merging it from here is rejected as tenant tampering; fork a row for this tenant instead.
			if (!Objects.equals(data.getTenantId(), this.tenantScopeFactory.getInstance().getTenant())) {
				isUpdate = false;
				data = null;
			}
		}
		if (data == null) {
			data = new UserSettingsEntity();
			data.setCreatedAt(Instant.now());
			data.setId(UUID.randomUUID());
		}

		// Settings are personal: the owner of an existing row is fixed, a new row belongs to the caller (or to
		// the user the caller explicitly names). Passing the owner as an OwnedResource means a plain user can only
		// touch their own rows, while EditUserSettings (administrators) may edit anyone's.
		UUID currentUserId = this.userScopeFactory.getInstance().getUserIdSafe();
		UUID owner = existingOwner != null ? existingOwner : (model.getEntityId() != null ? model.getEntityId() : currentUserId);
		this.authorizationService.authorizeAtLeastOneForce(owner != null ? List.of(new OwnedResource(owner)) : null, Permission.EditUserSettings);

        data.setName(model.getKey());
		data.setType(model.getType());
		data.setKey(model.getKey());
		data.setValue(model.getValue());
		data.setEntityId(owner);
		data.setUpdatedAt(Instant.now());

		if (isUpdate) this.tenantEntityManagerFactory.getInstance().merge(data);
		else this.tenantEntityManagerFactory.getInstance().persist(data);

		this.tenantEntityManagerFactory.getInstance().flush();

		return this.builderFactory.builder(UserSettingsBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, UserSettings._id, UserSettings._key), data);
	}

}
