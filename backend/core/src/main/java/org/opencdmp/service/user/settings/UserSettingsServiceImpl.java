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
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntityManager;
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
import java.util.UUID;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserSettingsServiceImpl.class));
	private final TenantEntityManager entityManager;
	private final AuthorizationService authorizationService;
	private final BuilderFactory builderFactory;
	private final ConventionService conventionService;
	private final MessageSource messageSource;


	@Autowired
	public UserSettingsServiceImpl(
			TenantEntityManager entityManager,
			AuthorizationService authorizationService,
			BuilderFactory builderFactory,
			ConventionService conventionService,
			MessageSource messageSource) {
		this.entityManager = entityManager;
		this.authorizationService = authorizationService;
		this.builderFactory = builderFactory;
		this.conventionService = conventionService;
		this.messageSource = messageSource;
	}

	@Override
	public UserSettings persist(UserSettingsPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting data access request").And("model", model).And("fields", fields));

		this.authorizationService.authorizeForce(Permission.EditUserSettings);

		boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		UserSettingsEntity data;
		if (isUpdate) {
			data = this.entityManager.find(UserSettingsEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), UserSettings.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		} else {
			data = new UserSettingsEntity();
			data.setCreatedAt(Instant.now());
			data.setId(UUID.randomUUID());

		}

        data.setName(model.getKey());
		data.setType(model.getType());
		data.setKey(model.getKey());
		data.setValue(model.getValue());
		data.setEntityId(model.getEntityId());
		data.setUpdatedAt(Instant.now());

		if (isUpdate) this.entityManager.merge(data);
		else this.entityManager.persist(data);

		this.entityManager.flush();

		return this.builderFactory.builder(UserSettingsBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, UserSettings._id, UserSettings._key), data);
	}

}
