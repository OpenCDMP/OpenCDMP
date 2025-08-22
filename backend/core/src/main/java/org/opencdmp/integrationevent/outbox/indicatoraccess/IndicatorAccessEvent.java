package org.opencdmp.integrationevent.outbox.indicatoraccess;

import gr.cite.tools.validation.ValidatorFactory;
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

import java.util.*;

public class IndicatorAccessEvent extends TrackedEvent {

	private UUID userId;
	public static final String _userId= "userId";

	private UUID indicatorId;
	public static final String _indicatorId= "indicatorId";

	private IndicatorAccessConfig config;
	public static final String _config = "config";

	private boolean toDelete;

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getIndicatorId() {
		return indicatorId;
	}

	public void setIndicatorId(UUID indicatorId) {
		this.indicatorId = indicatorId;
	}

	public IndicatorAccessConfig getConfig() {
		return config;
	}

	public void setConfig(IndicatorAccessConfig config) {
		this.config = config;
	}

	public boolean isToDelete() {
		return toDelete;
	}

	public void setToDelete(boolean toDelete) {
		this.toDelete = toDelete;
	}

	@Component(IndicatorAccessEventValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IndicatorAccessEventValidator extends BaseValidator<IndicatorAccessEvent> {

		public static final String ValidatorName = "IndicatorAccessEventValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;

		protected IndicatorAccessEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<IndicatorAccessEvent> modelClass() {
			return IndicatorAccessEvent.class;
		}

		@Override
		protected List<Specification> specifications(IndicatorAccessEvent item) {
			return Arrays.asList(
					this.spec()
							.must(() -> this.isValidGuid(item.getIndicatorId()))
							.failOn(IndicatorAccessEvent._indicatorId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{IndicatorAccessEvent._indicatorId}, LocaleContextHolder.getLocale())),
					this.refSpec()
							.iff(() -> !this.isNull(item.getConfig()))
							.on(IndicatorAccessEvent._config)
							.over(item.getConfig())
							.using(() -> this.validatorFactory.validator(IndicatorAccessConfig.IndicatorAccessConfigValidator.class))
			);
		}
	}
}
