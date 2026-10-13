package org.opencdmp.integrationevent.outbox.dashboardtouched;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.TrackedEvent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DashboardTouchedEvent extends TrackedEvent {

	public static final short ENTITY_TYPE_USER = 0;
	public static final short ENTITY_TYPE_TENANT = 1;
	public static final short ENTITY_TYPE_APPLICATION = 2;

	private String name;
	public static final String _name = "name";

	private String value;
	public static final String _value = "value";

	private UUID entityId;
	public static final String _entityId = "entityId";

	private String key;
	public static final String _key = "key";

	private Short entityType;
	public static final String _entityType = "entityType";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Short getEntityType() {
		return entityType;
	}

	public void setEntityType(Short entityType) {
		this.entityType = entityType;
	}

	@Component(DashboardTouchedEventValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class DashboardTouchedEventValidator extends BaseValidator<DashboardTouchedEvent> {

		public static final String ValidatorName = "DashboardTouchedEventValidator";

		private final MessageSource messageSource;

		protected DashboardTouchedEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<DashboardTouchedEvent> modelClass() {
			return DashboardTouchedEvent.class;
		}

		@Override
		protected List<Specification> specifications(DashboardTouchedEvent item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isNull(item.getValue()))
							.failOn(DashboardTouchedEvent._value).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DashboardTouchedEvent._value}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isNull(item.getName()))
							.failOn(DashboardTouchedEvent._name).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DashboardTouchedEvent._name}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isNull(item.getEntityType()))
							.failOn(DashboardTouchedEvent._entityType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DashboardTouchedEvent._entityType}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isNull(item.getKey()))
							.failOn(DashboardTouchedEvent._key).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DashboardTouchedEvent._key}, LocaleContextHolder.getLocale())),
					this.spec()
							.iff(() -> item.getEntityType() != null && item.getEntityType() != ENTITY_TYPE_APPLICATION)
							.must(() -> !this.isNull(item.getEntityId()))
							.failOn(DashboardTouchedEvent._entityId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DashboardTouchedEvent._entityId}, LocaleContextHolder.getLocale()))
			);
		}
	}
}
