package org.opencdmp.model.persist.descriptiontemplatedefinition;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.BaseFieldDataPersist;
import org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class FieldPersist {

    private String id;

    public static final String _id = "id";

    private Integer ordinal;

    public static final String _ordinal = "ordinal";

    private List<String> semantics;

    private DefaultValuePersist defaultValue;
    public static final String _defaultValue = "defaultValue";

    private List<RulePersist> visibilityRules;

    public static final String _visibilityRules = "visibilityRules";

    private List<FieldValidationType> validations;

    private Boolean includeInExport;

    public static final String _includeInExport = "includeInExport";

    private BaseFieldDataPersist data;

    public static final String _data = "data";

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public List<String> getSemantics() {
        return this.semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public DefaultValuePersist getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(DefaultValuePersist defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<RulePersist> getVisibilityRules() {
        return this.visibilityRules;
    }

    public void setVisibilityRules(List<RulePersist> visibilityRules) {
        this.visibilityRules = visibilityRules;
    }

    public List<FieldValidationType> getValidations() {
        return this.validations;
    }

    public void setValidations(List<FieldValidationType> validations) {
        this.validations = validations;
    }

    public Boolean getIncludeInExport() {
        return this.includeInExport;
    }

    public void setIncludeInExport(Boolean includeInExport) {
        this.includeInExport = includeInExport;
    }

    public BaseFieldDataPersist getData() {
        return this.data;
    }

    public void setData(BaseFieldDataPersist data) {
        this.data = data;
    }

    @Component(FieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class FieldPersistValidator extends BaseValidator<FieldPersist> {

        public static final String ValidatorName = "DescriptionTemplate.FieldPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;
        private final FieldDataHelperServiceProvider fieldDataHelperServiceProvider;
        private String fieldSetId;

        protected FieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory, FieldDataHelperServiceProvider fieldDataHelperServiceProvider) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
	        this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
        }

        public FieldPersistValidator withFieldSetId(String fieldSetId) {
            this.fieldSetId = fieldSetId;
            return this;
        }

        @Override
        protected Class<FieldPersist> modelClass() {
            return FieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(FieldPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(FieldPersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(FieldPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getIncludeInExport()))
                            .failOn(FieldPersist._includeInExport).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._includeInExport}, LocaleContextHolder.getLocale())),

                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getVisibilityRules()))
                            .on(FieldPersist._visibilityRules)
                            .over(item.getVisibilityRules())
                            .using((itm) -> this.validatorFactory.validator(RulePersist.RulePersistValidator.class).withFieldPersist(item).withFieldSetId(fieldSetId)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefaultValue()))
                            .on(FieldPersist._defaultValue)
                            .over(item.getDefaultValue())
                            .using(() -> this.validatorFactory.validator(DefaultValuePersist.DefaultValuePersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getData()) && item.getData().getFieldType() != null)
                            .on(FieldPersist._data)
                            .over(item.getData())
                            .using(() -> this.fieldDataHelperServiceProvider.get(item.getData().getFieldType()).getPersistModelValidator()),
                    this.spec()
                            .iff(() -> !this.isNull(item.getData()))
                            .must(() -> !this.isNull(item.getData().getFieldType()))
                            .failOn(FieldPersist._data + '.' + BaseFieldDataPersist._fieldType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{BaseFieldDataPersist._fieldType}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}
