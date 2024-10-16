package org.opencdmp.model.persist.descriptiontemplatedefinition;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
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

public class PagePersist {

    private String id = null;

    public static final String _id = "id";

    private Integer ordinal = null;

    public static final String _ordinal = "ordinal";

    private String title = null;

    public static final String _title = "title";

    private List<SectionPersist> sections = null;

    public static final String _sections = "sections";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public List<SectionPersist> getSections() {
        return sections;
    }

    public void setSections(List<SectionPersist> sections) {
        this.sections = sections;
    }

    @Component(PagePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PagePersistValidator extends BaseValidator<PagePersist> {

        public static final String ValidatorName = "DescriptionTemplate.PagePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PagePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
	        this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PagePersist> modelClass() {
            return PagePersist.class;
        }

        @Override
        protected List<Specification> specifications(PagePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getSections()))
                            .failOn(PagePersist._sections).failWith(messageSource.getMessage("Validation_Required", new Object[]{PagePersist._sections}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSections()))
                            .on(PagePersist._sections)
                            .over(item.getSections())
                            .using((itm) -> this.validatorFactory.validator(SectionPersist.SectionPersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(PagePersist._id).failWith(messageSource.getMessage("Validation_Required", new Object[]{PagePersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(PagePersist._ordinal).failWith(messageSource.getMessage("Validation_Required", new Object[]{PagePersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTitle()))
                            .failOn(PagePersist._title).failWith(messageSource.getMessage("Validation_Required", new Object[]{PagePersist._title}, LocaleContextHolder.getLocale()))
            );
        }
    }

}


