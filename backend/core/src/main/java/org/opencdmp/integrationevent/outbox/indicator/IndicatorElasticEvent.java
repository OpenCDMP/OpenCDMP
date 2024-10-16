package org.opencdmp.integrationevent.outbox.indicator;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.TrackedEvent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IndicatorElasticEvent extends TrackedEvent {

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

	@Component(IndicatorElasticEventValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IndicatorElasticEventValidator extends BaseValidator<IndicatorElasticEvent> {

		public static final String ValidatorName = "IndicatorElasticEventValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;

		protected IndicatorElasticEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<IndicatorElasticEvent> modelClass() {
			return IndicatorElasticEvent.class;
		}

		@Override
		protected List<Specification> specifications(IndicatorElasticEvent item) {
			return Arrays.asList(
					this.refSpec()
							.iff(() -> !this.isNull(item.getSchema()))
							.on(IndicatorElasticEvent._schema)
							.over(item.getSchema())
							.using(() -> this.validatorFactory.validator(IndicatorSchema.IndicatorSchemaValidator.class))
			);
		}
	}
}
