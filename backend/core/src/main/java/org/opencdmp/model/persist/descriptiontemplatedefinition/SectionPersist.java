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

public class SectionPersist {

    private String id;

    public static final String _id = "id";

    private Integer ordinal;

    public static final String _ordinal = "ordinal";

    private String title;

    public static final String _title = "title";

    private String description;

    public static final String _description = "description";

    private List<SectionPersist> sections;

    public static final String _sections = "sections";

    private List<FieldSetPersist> fieldSets;

    public static final String _fieldSets = "fieldSets";

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
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

    public List<SectionPersist> getSections() {
        return this.sections;
    }

    public void setSections(List<SectionPersist> sections) {
        this.sections = sections;
    }

    public List<FieldSetPersist> getFieldSets() {
        return this.fieldSets;
    }

    public void setFieldSets(List<FieldSetPersist> fieldSets) {
        this.fieldSets = fieldSets;
    }

    @Component(SectionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SectionPersistValidator extends BaseValidator<SectionPersist> {

        public static final String ValidatorName = "DescriptionTemplate.SectionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected SectionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<SectionPersist> modelClass() {
            return SectionPersist.class;
        }

        @Override
        protected List<Specification> specifications(SectionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(SectionPersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(SectionPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTitle()))
                            .failOn(SectionPersist._title).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._title}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSections()))
                            .on(SectionPersist._sections)
                            .over(item.getSections())
                            .using((itm) -> this.validatorFactory.validator(SectionPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFieldSets()))
                            .on(SectionPersist._fieldSets)
                            .over(item.getFieldSets())
                            .using((itm) -> this.validatorFactory.validator(FieldSetPersist.FieldSetPersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getFieldSets()) || !this.isListNullOrEmpty(item.getSections()))
                            .failOn(SectionPersist._fieldSets).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._fieldSets}, LocaleContextHolder.getLocale()))
                    );
        }
    }

}


