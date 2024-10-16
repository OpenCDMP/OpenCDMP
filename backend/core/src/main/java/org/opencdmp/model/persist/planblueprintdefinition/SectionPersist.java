package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.exception.MyApplicationException;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class SectionPersist {

    private UUID id;

    public static final String _id = "id";

    private String description;

    private String label;

    public static final String _label = "label";

    private Integer ordinal;

    public static final String _ordinal = "ordinal";

    private Boolean hasTemplates;

    public static final String _hasTemplates = "hasTemplates";

    private List<FieldPersist> fields;

    public static final String _fields = "fields";

    private List<DescriptionTemplatePersist> descriptionTemplates;

    public static final String _descriptionTemplates = "descriptionTemplates";

    private Boolean prefillingSourcesEnabled;
    public static final String _prefillingSourcesEnabled = "prefillingSourcesEnabled";

    private List<UUID> prefillingSourcesIds;

    public static final String _prefillingSourcesIds = "prefillingSourcesIds";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public Boolean getHasTemplates() {
        return this.hasTemplates;
    }

    public void setHasTemplates(Boolean hasTemplates) {
        this.hasTemplates = hasTemplates;
    }

    public List<FieldPersist> getFields() {
        return this.fields;
    }

    public void setFields(List<FieldPersist> fields) {
        this.fields = fields;
    }

    public List<DescriptionTemplatePersist> getDescriptionTemplates() {
        return this.descriptionTemplates;
    }

    public void setDescriptionTemplates(List<DescriptionTemplatePersist> descriptionTemplates) { this.descriptionTemplates = descriptionTemplates; }

    public List<UUID> getPrefillingSourcesIds() {
        return this.prefillingSourcesIds;
    }

    public void setPrefillingSourcesIds(List<UUID> prefillingSourcesIds) { this.prefillingSourcesIds = prefillingSourcesIds; }

    public Boolean getPrefillingSourcesEnabled() { return this.prefillingSourcesEnabled; }

    public void setPrefillingSourcesEnabled(Boolean prefillingSourcesEnabled) { this.prefillingSourcesEnabled = prefillingSourcesEnabled; }

    @Component(SectionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SectionPersistValidator extends BaseValidator<SectionPersist> {

        public static final String ValidatorName = "PlanBlueprint.SectionPersistValidator";

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
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(SectionPersist._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(SectionPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(SectionPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getHasTemplates()))
                            .failOn(SectionPersist._hasTemplates).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._hasTemplates}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFields()))
                            .on(SectionPersist._fields)
                            .over(item.getFields())
                            .using((itm) -> {
                                switch (((FieldPersist) itm).getCategory()){
                                    case Extra -> {
                                        return this.validatorFactory.validator(ExtraFieldPersist.ExtraFieldPersistValidator.class);
                                    }
                                    case System -> {
                                        return this.validatorFactory.validator(SystemFieldPersist.SystemFieldPersistValidator.class);
                                    }
                                    case ReferenceType -> {
                                        return this.validatorFactory.validator(ReferenceTypeFieldPersist.ReferenceFieldPersistPersistValidator.class);
                                    }
                                    default -> throw new MyApplicationException("unrecognized type " + ((FieldPersist) itm).getCategory());
                                }
                            }),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .on(SectionPersist._descriptionTemplates)
                            .over(item.getDescriptionTemplates())
                            .using((itm) -> this.validatorFactory.validator(DescriptionTemplatePersist.DescriptionTemplatePersistValidator.class)),
                    this.spec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .must(() -> item.getDescriptionTemplates().stream().map(DescriptionTemplatePersist::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList()).size() == item.getDescriptionTemplates().size())
                            .failOn(SectionPersist._descriptionTemplates).failWith(this.messageSource.getMessage("Validation_Unique", new Object[]{SectionPersist._descriptionTemplates}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .must(() -> !this.isNull(item.getPrefillingSourcesEnabled()))
                            .failOn(SectionPersist._prefillingSourcesEnabled).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SectionPersist._prefillingSourcesEnabled}, LocaleContextHolder.getLocale()))

            );
        }
    }

}


