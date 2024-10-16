package org.opencdmp.model.persist.referencetypedefinition;

import org.opencdmp.commons.enums.ReferenceFieldDataType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.DescriptionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ReferenceTypeFieldPersist {

    private String code = null;

    public static final String _code = "code";

    private String label = null;

    public static final String _label = "label";

    private String description;

    private ReferenceFieldDataType dataType;

    public static final String _dataType = "dataType";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReferenceFieldDataType getDataType() {
        return dataType;
    }

    public void setDataType(ReferenceFieldDataType dataType) {
        this.dataType = dataType;
    }

    @Component(ReferenceTypeFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferenceTypeFieldPersistValidator extends BaseValidator<ReferenceTypeFieldPersist> {

        public static final String ValidatorName = "ReferenceTypeFieldPersistValidator";

        private final MessageSource messageSource;

        protected ReferenceTypeFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<ReferenceTypeFieldPersist> modelClass() {
            return ReferenceTypeFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferenceTypeFieldPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(ReferenceTypeFieldPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeFieldPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(ReferenceTypeFieldPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeFieldPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getDataType()))
                            .failOn(ReferenceTypeFieldPersist._dataType).failWith(messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypeFieldPersist._dataType}, LocaleContextHolder.getLocale()))
            );
        }
    }

}


