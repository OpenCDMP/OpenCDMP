package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptiontemplatedefinition.FieldSetPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.SectionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RootSectionPersist extends SectionPersist {

    private Boolean hasTemplates;
    public static final String _hasTemplates = "hasTemplates";

    private List<BlueprintDescriptionTemplatePersist> descriptionTemplates;
    public static final String _descriptionTemplates = "descriptionTemplates";

    private Boolean prefillingSourcesEnabled;
    public static final String _prefillingSourcesEnabled = "prefillingSourcesEnabled";

    private Boolean canEditDescriptionTemplates;
    public static final String _canEditDescriptionTemplates = "canEditDescriptionTemplates";

    private List<UUID> prefillingSourcesIds;
    public static final String _prefillingSourcesIds = "prefillingSourcesIds";

    public Boolean getHasTemplates() {
        return hasTemplates;
    }

    public void setHasTemplates(Boolean hasTemplates) {
        this.hasTemplates = hasTemplates;
    }

    public List<BlueprintDescriptionTemplatePersist> getDescriptionTemplates() {
        return descriptionTemplates;
    }

    public void setDescriptionTemplates(List<BlueprintDescriptionTemplatePersist> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public Boolean getPrefillingSourcesEnabled() {
        return prefillingSourcesEnabled;
    }

    public void setPrefillingSourcesEnabled(Boolean prefillingSourcesEnabled) {
        this.prefillingSourcesEnabled = prefillingSourcesEnabled;
    }

    public Boolean getCanEditDescriptionTemplates() {
        return canEditDescriptionTemplates;
    }

    public void setCanEditDescriptionTemplates(Boolean canEditDescriptionTemplates) {
        this.canEditDescriptionTemplates = canEditDescriptionTemplates;
    }

    public List<UUID> getPrefillingSourcesIds() {
        return prefillingSourcesIds;
    }

    public void setPrefillingSourcesIds(List<UUID> prefillingSourcesIds) {
        this.prefillingSourcesIds = prefillingSourcesIds;
    }

    @Component(RootSectionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RootSectionPersistValidator extends BaseValidator<RootSectionPersist> {

        public static final String ValidatorName = "PlanBlueprint.RootSectionPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected RootSectionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors,
                                           MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<RootSectionPersist> modelClass() {
            return RootSectionPersist.class;
        }

        @Override
        protected List<Specification> specifications(RootSectionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(RootSectionPersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTitle()))
                            .failOn(RootSectionPersist._title).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._title}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(RootSectionPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getHasTemplates()))
                            .failOn(RootSectionPersist._hasTemplates).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._hasTemplates}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSections()))
                            .on(RootSectionPersist._sections)
                            .over(item.getSections())
                            .using((itm) -> this.validatorFactory.validator(SectionPersist.SectionPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFieldSets()))
                            .on(RootSectionPersist._fieldSets)
                            .over(item.getFieldSets())
                            .using((itm) -> this.validatorFactory.validator(FieldSetPersist.FieldSetPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .on(RootSectionPersist._descriptionTemplates)
                            .over(item.getDescriptionTemplates())
                            .using((itm) -> this.validatorFactory.validator(BlueprintDescriptionTemplatePersist.DescriptionTemplatePersistValidator.class)),
                    this.spec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .must(() -> item.getDescriptionTemplates().stream().map(BlueprintDescriptionTemplatePersist::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()).size() == item.getDescriptionTemplates().size())
                            .failOn(RootSectionPersist._descriptionTemplates).failWith(this.messageSource.getMessage("Validation_Unique", new Object[]{RootSectionPersist._descriptionTemplates}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getHasTemplates)
                            .must(() -> !this.isNull(item.getPrefillingSourcesEnabled()))
                            .failOn(RootSectionPersist._prefillingSourcesEnabled).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._prefillingSourcesEnabled}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getHasTemplates)
                            .must(() -> !this.isNull(item.getCanEditDescriptionTemplates()))
                            .failOn(RootSectionPersist._canEditDescriptionTemplates).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._canEditDescriptionTemplates}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getHasTemplates() && !this.isNull(item.getCanEditDescriptionTemplates()) && !item.getCanEditDescriptionTemplates())
                            .must(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .failOn(RootSectionPersist._descriptionTemplates).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RootSectionPersist._descriptionTemplates}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
