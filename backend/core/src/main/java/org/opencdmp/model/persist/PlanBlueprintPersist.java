package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import jakarta.validation.constraints.Size;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.planblueprintdefinition.DefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlanBlueprintPersist {

    private UUID id;

    public static final String _id = "id";

    @Size(max = PlanBlueprintEntity._labelLength, message = "{validation.largerthanmax}")
    private String label;

    public static final String _label = "label";

    private String code;

    public final static String _code = "code";

    private DefinitionPersist definition;

    public static final String _definition = "definition";

    private PlanBlueprintStatus status;

    public static final String _status = "status";

    private String hash;

    public static final String _hash = "hash";

    private String description;

    public static final String _description = "description";

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DefinitionPersist getDefinition() {
        return this.definition;
    }

    public void setDefinition(DefinitionPersist definition) {
        this.definition = definition;
    }

    public PlanBlueprintStatus getStatus() {
        return this.status;
    }

    public void setStatus(PlanBlueprintStatus status) {
        this.status = status;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Component(PlanBlueprintPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanBlueprintPersistValidator extends BaseValidator<PlanBlueprintPersist> {

        public static final String ValidatorName = "PlanBlueprintPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanBlueprintPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanBlueprintPersist> modelClass() {
            return PlanBlueprintPersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanBlueprintPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(PlanBlueprintPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(PlanBlueprintPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(PlanBlueprintPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), PlanBlueprintEntity._labelLength))
                            .failOn(PlanBlueprintPersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{PlanBlueprintPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(PlanBlueprintPersist._status).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintPersist._status}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(PlanBlueprintPersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), PlanBlueprintEntity._codeLength))
                            .failOn(PlanBlueprintPersist._code).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{PlanBlueprintPersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getStatus() == PlanBlueprintStatus.Finalized)
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(PlanBlueprintPersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PlanBlueprintPersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(PlanBlueprintPersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class))
            );
        }
    }

}



