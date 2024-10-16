package org.opencdmp.model.persist.usagelimit;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.UsageLimitPeriodicityRange;
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

public class DefinitionPersist {

	private Boolean hasPeriodicity;
	public final static String _hasPeriodicity = "hasPeriodicity";

	private UsageLimitPeriodicityRange periodicityRange;
	public final static String _periodicityRange = "periodicityRange";

	public Boolean getHasPeriodicity() {
		return hasPeriodicity;
	}

	public void setHasPeriodicity(Boolean hasPeriodicity) {
		this.hasPeriodicity = hasPeriodicity;
	}

	public UsageLimitPeriodicityRange getPeriodicityRange() {
		return periodicityRange;
	}

	public void setPeriodicityRange(UsageLimitPeriodicityRange periodicityRange) {
		this.periodicityRange = periodicityRange;
	}

	@Component(DefinitionPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class DefinitionPersistValidator extends BaseValidator<DefinitionPersist> {

		public static final String ValidatorName = "UsageLimit.DefinitionPersistValidator";

		private final MessageSource messageSource;


		protected DefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
            this.messageSource = messageSource;
        }

		@Override
		protected Class<DefinitionPersist> modelClass() {
			return DefinitionPersist.class;
		}

		@Override
		protected List<Specification> specifications(DefinitionPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isNull(item.getHasPeriodicity()))
							.failOn(DefinitionPersist._hasPeriodicity).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DefinitionPersist._hasPeriodicity}, LocaleContextHolder.getLocale())),
					this.spec()
							.iff(() -> !this.isNull(item.getHasPeriodicity()) && item.getHasPeriodicity())
							.must(() -> !this.isNull(item.getPeriodicityRange()))
							.failOn(DefinitionPersist._periodicityRange).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DefinitionPersist._periodicityRange}, LocaleContextHolder.getLocale()))
			);
		}


	}

}
