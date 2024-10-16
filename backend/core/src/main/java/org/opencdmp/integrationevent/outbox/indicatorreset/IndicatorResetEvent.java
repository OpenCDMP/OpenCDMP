package org.opencdmp.integrationevent.outbox.indicatorreset;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.TrackedEvent;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorMetadata;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorSchema;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IndicatorResetEvent extends TrackedEvent {

	private UUID id;

	private IndicatorMetadata metadata;

	private IndicatorSchema schema;
	public static final String _schema = "schema";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public IndicatorMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(IndicatorMetadata metadata) {
		this.metadata = metadata;
	}

	public IndicatorSchema getSchema() {
		return schema;
	}

	public void setSchema(IndicatorSchema schema) {
		this.schema = schema;
	}

	@Component(IndicatorResetEventValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IndicatorResetEventValidator extends BaseValidator<IndicatorResetEvent> {

		public static final String ValidatorName = "IndicatorResetEventValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;

		protected IndicatorResetEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<IndicatorResetEvent> modelClass() {
			return IndicatorResetEvent.class;
		}

		@Override
		protected List<Specification> specifications(IndicatorResetEvent item) {
			return Arrays.asList(
					this.refSpec()
							.iff(() -> !this.isNull(item.getSchema()))
							.on(IndicatorResetEvent._schema)
							.over(item.getSchema())
							.using(() -> this.validatorFactory.validator(IndicatorSchema.IndicatorSchemaValidator.class))
			);
		}
	}
}
