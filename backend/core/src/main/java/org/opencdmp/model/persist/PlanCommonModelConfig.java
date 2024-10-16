package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlanCommonModelConfig {

    private UUID fileId;
    public static final String _fileId = "fileId";

    private String label;
    public static final String _label = "label";

    private String repositoryId;
    public static final String _repositoryId = "repositoryId";

    private UUID blueprintId;
    public static final String _blueprintId = "blueprintId";

    private List<DescriptionCommonModelConfig> descriptions;
    public static final String _descriptions = "descriptions";

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public UUID getBlueprintId() {
        return blueprintId;
    }

    public void setBlueprintId(UUID blueprintId) {
        this.blueprintId = blueprintId;
    }

    public List<DescriptionCommonModelConfig> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<DescriptionCommonModelConfig> descriptions) {
        this.descriptions = descriptions;
    }

    @Component(PlanCommonModelConfigValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanCommonModelConfigValidator extends BaseValidator<PlanCommonModelConfig> {

        public static final String ValidatorName = "PlanCommonModelConfigValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected PlanCommonModelConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanCommonModelConfig> modelClass() {
            return PlanCommonModelConfig.class;
        }

        @Override
        protected List<Specification> specifications(PlanCommonModelConfig item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getFileId()))
                            .failOn(PlanCommonModelConfig._fileId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanCommonModelConfig._fileId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(PlanCommonModelConfig._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanCommonModelConfig._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), PlanEntity._labelLength))
                            .failOn(PlanCommonModelConfig._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{PlanCommonModelConfig._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getRepositoryId()))
                            .failOn(PlanCommonModelConfig._repositoryId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanCommonModelConfig._repositoryId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getRepositoryId()))
                            .must(() -> this.isValidGuid(UUID.fromString(item.getRepositoryId())))
                            .failOn(PlanCommonModelConfig._repositoryId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanCommonModelConfig._repositoryId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getBlueprintId()))
                            .failOn(PlanCommonModelConfig._blueprintId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanCommonModelConfig._blueprintId}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptions()))
                            .on(PlanCommonModelConfig._descriptions)
                            .over(item.getDescriptions())
                            .using((itm) -> this.validatorFactory.validator(DescriptionCommonModelConfig.DescriptionCommonModelConfigValidator.class))
            );
        }
    }
}
