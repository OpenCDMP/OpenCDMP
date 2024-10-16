package org.opencdmp.model.persist.referencedefinition;

import org.opencdmp.commons.enums.ReferenceFieldDataType;
import org.opencdmp.commons.validation.BaseValidator;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class FieldPersist {

	private String code = null;

	public static final String _code = "code";

	private ReferenceFieldDataType dataType;

	public static final String _dataType = "dataType";

	private String value = null;

	public static final String _value = "value";

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ReferenceFieldDataType getDataType() {
		return dataType;
	}

	public void setDataType(ReferenceFieldDataType dataType) {
		this.dataType = dataType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Component(FieldPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class FieldPersistValidator extends BaseValidator<FieldPersist> {

		public static final String ValidatorName = "Reference.FieldPersistValidator";

		private final MessageSource messageSource;

		protected FieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
            this.messageSource = messageSource;
        }

		@Override
		protected Class<FieldPersist> modelClass() {
			return FieldPersist.class;
		}

		@Override
		protected List<Specification> specifications(FieldPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isEmpty(item.getCode()))
							.failOn(FieldPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._code}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isNull(item.getDataType()))
							.failOn(FieldPersist._dataType).failWith(messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._dataType}, LocaleContextHolder.getLocale()))
			);
		}
	}

}


