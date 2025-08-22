package org.opencdmp.integrationevent.outbox.indicatorpoint;

import com.fasterxml.jackson.annotation.JsonAnySetter;
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

public class IndicatorPointEvent extends TrackedEvent {
	public IndicatorPointEvent() {
		Map<String, Object> map = new HashMap<>();
		properties = new ArrayList<>();
	}

	private UUID indicatorId;
	public static final String _indicatorId= "indicatorId";

	private Date timestamp;

	private String batchId;

	private String groupHash;

	private Date batchTimestamp;

	private List<Map<String, Object>> properties;

	public UUID getIndicatorId() {
		return indicatorId;
	}

	public void setIndicatorId(UUID indicatorId) {
		this.indicatorId = indicatorId;
	}

	public Date getBatchTimestamp() {
		return batchTimestamp;
	}

	public void setBatchTimestamp(Date batchTimestamp) {
		this.batchTimestamp = batchTimestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getGroupHash() {
		return groupHash;
	}

	public void setGroupHash(String groupHash) {
		this.groupHash = groupHash;
	}

	public List<Map<String, Object>> getProperties() {
		return properties;
	}

	public void setProperties(List<Map<String, Object>> properties) {
		this.properties = properties;
	}

	@Component(IndicatorPointEventValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IndicatorPointEventValidator extends BaseValidator<IndicatorPointEvent> {

		public static final String ValidatorName = "IndicatorPointEventValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;

		protected IndicatorPointEventValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<IndicatorPointEvent> modelClass() {
			return IndicatorPointEvent.class;
		}

		@Override
		protected List<Specification> specifications(IndicatorPointEvent item) {
			return Arrays.asList(
					this.spec()
							.must(() -> this.isValidGuid(item.getIndicatorId()))
							.failOn(IndicatorPointEvent._indicatorId).failWith(messageSource.getMessage("Validation_Required", new Object[]{IndicatorPointEvent._indicatorId}, LocaleContextHolder.getLocale()))
			);
		}
	}
}
