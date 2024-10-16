package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.prefillingsourcedefinition.PrefillingSourceDefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PrefillingSourcePersist {

    private UUID id;

    private String label;

    public static final String _label = "label";

    private String code;
    public static final String _code = "code";
    

    private PrefillingSourceDefinitionPersist definition;

    public static final String _definition = "definition";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PrefillingSourceDefinitionPersist getDefinition() {
        return this.definition;
    }

    public void setDefinition(PrefillingSourceDefinitionPersist definition) {
        this.definition = definition;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(PrefillingSourcePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PrefillingSourcePersistValidator extends BaseValidator<PrefillingSourcePersist> {

        public static final String ValidatorName = "PrefillingSourcePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PrefillingSourcePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PrefillingSourcePersist> modelClass() {
            return PrefillingSourcePersist.class;
        }

        @Override
        protected List<Specification> specifications(PrefillingSourcePersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(PrefillingSourcePersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourcePersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(PrefillingSourcePersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(PrefillingSourcePersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourcePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(PrefillingSourcePersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourcePersist._definition}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(PrefillingSourcePersist._code).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{PrefillingSourcePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), ReferenceTypeEntity._codeLength))
                            .failOn(PrefillingSourcePersist._code).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{PrefillingSourcePersist._code}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(PrefillingSourcePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(PrefillingSourceDefinitionPersist.PrefillingSourceDefinitionPersistValidator.class))
            );
        }
    }

}
