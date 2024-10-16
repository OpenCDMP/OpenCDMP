package org.opencdmp.integrationevent.outbox.indicatoraccess;


import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class FilterColumnConfig {

	private String column;
	public static final String _column = "column";

	private List<String> values;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@Component(FilterColumnConfigValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class FilterColumnConfigValidator extends BaseValidator<FilterColumnConfig> {

		public static final String ValidatorName = "Indicatoraccess.FilterColumnConfigValidator";

		private final MessageSource messageSource;

		protected FilterColumnConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<FilterColumnConfig> modelClass() {
			return FilterColumnConfig.class;
		}

		@Override
		protected List<Specification> specifications(FilterColumnConfig item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isEmpty(item.getColumn()))
							.failOn(FilterColumnConfig._column).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FilterColumnConfig._column}, LocaleContextHolder.getLocale()))
			);
		}
	}
}
