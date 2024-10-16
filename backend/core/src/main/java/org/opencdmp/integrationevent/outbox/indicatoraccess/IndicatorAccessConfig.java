package org.opencdmp.integrationevent.outbox.indicatoraccess;


import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class IndicatorAccessConfig {

	private List<FilterColumnConfig> globalFilterColumns;
	public static final String _globalFilterColumns = "globalFilterColumns";


	private Map<UUID, List<FilterColumnConfig>> groupFilterColumns;
	public static final String _groupFilterColumns = "groupFilterColumns";

	public List<FilterColumnConfig> getGlobalFilterColumns() {
		return globalFilterColumns;
	}

	public void setGlobalFilterColumns(List<FilterColumnConfig> globalFilterColumns) {
		this.globalFilterColumns = globalFilterColumns;
	}

	public Map<UUID, List<FilterColumnConfig>> getGroupFilterColumns() {
		return groupFilterColumns;
	}

	public void setGroupFilterColumns(Map<UUID, List<FilterColumnConfig>> groupFilterColumns) {
		this.groupFilterColumns = groupFilterColumns;
	}

	@Component(IndicatorAccessConfigValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IndicatorAccessConfigValidator extends BaseValidator<IndicatorAccessConfig> {

		public static final String ValidatorName = "IndicatorAccessConfigValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;

		protected IndicatorAccessConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

		@Override
		protected Class<IndicatorAccessConfig> modelClass() {
			return IndicatorAccessConfig.class;
		}

		@Override
		protected List<Specification> specifications(IndicatorAccessConfig item) {
			return Arrays.asList(
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getGlobalFilterColumns()))
							.on(IndicatorAccessConfig._globalFilterColumns)
							.over(item.getGlobalFilterColumns())
							.using((itm) -> this.validatorFactory.validator(FilterColumnConfig.FilterColumnConfigValidator.class))
			);
		}
	}
}
