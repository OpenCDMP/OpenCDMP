package org.opencdmp.model.persist.descriptiontemplatedefinition;

import gr.cite.tools.validation.ValidatorFactory;
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

public class FieldSetPersist {

    private String id;

    public static final String _id = "id";

    private Integer ordinal;

    public static final String _ordinal = "ordinal";

    private String title;

    public static final String _title = "title";

    private String description;

    public static final String _description = "description";

    private String extendedDescription;
    public static final String _extendedDescription = "extendedDescription";

    private String additionalInformation;
    public static final String _additionalInformation = "additionalInformation";

    private MultiplicityPersist multiplicity;
    public static final String _multiplicity = "multiplicity";

    private Boolean hasMultiplicity;
    public static final String _hasMultiplicity = "hasMultiplicity";
    

    private Boolean hasCommentField;

    private List<FieldPersist> fields;

    public static final String _fields = "fields";

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

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtendedDescription() {
        return this.extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getAdditionalInformation() {
        return this.additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public MultiplicityPersist getMultiplicity() {
        return this.multiplicity;
    }

    public void setMultiplicity(MultiplicityPersist multiplicity) {
        this.multiplicity = multiplicity;
    }

    public Boolean getHasCommentField() {
        return this.hasCommentField;
    }

    public void setHasCommentField(Boolean hasCommentField) {
        this.hasCommentField = hasCommentField;
    }

    public List<FieldPersist> getFields() {
        return this.fields;
    }

    public void setFields(List<FieldPersist> fields) {
        this.fields = fields;
    }

    public Boolean getHasMultiplicity() {
        return this.hasMultiplicity;
    }

    public void setHasMultiplicity(Boolean hasMultiplicity) {
        this.hasMultiplicity = hasMultiplicity;
    }

    @Component(FieldSetPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class FieldSetPersistValidator extends BaseValidator<FieldSetPersist> {

        public static final String ValidatorName = "DescriptionTemplate.FieldSetPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected FieldSetPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<FieldSetPersist> modelClass() {
            return FieldSetPersist.class;
        }

        @Override
        protected List<Specification> specifications(FieldSetPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(FieldSetPersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldSetPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(FieldSetPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldSetPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getHasMultiplicity()))
                            .failOn(FieldSetPersist._hasMultiplicity).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldSetPersist._hasMultiplicity}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTitle()))
                            .failOn(FieldSetPersist._title).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldSetPersist._title}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getMultiplicity()))
                            .on(FieldSetPersist._multiplicity)
                            .over(item.getMultiplicity())
                            .using(() -> this.validatorFactory.validator(MultiplicityPersist.MultiplicityValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFields()))
                            .on(FieldSetPersist._fields)
                            .over(item.getFields())
                            .using((itm) -> this.validatorFactory.validator(FieldPersist.FieldPersistValidator.class).withFieldSetId(item.getId()))
            );
        }
    }

}
