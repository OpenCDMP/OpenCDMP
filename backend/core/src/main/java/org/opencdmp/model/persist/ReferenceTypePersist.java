package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.referencetypedefinition.ReferenceTypeDefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReferenceTypePersist {

    private UUID id;

    private String name;

    public static final String _name = "name";

    private String code;

    public static final String _code = "code";

    private ReferenceTypeDefinitionPersist definition;

    public static final String _definition = "definition";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ReferenceTypeDefinitionPersist getDefinition() {
        return this.definition;
    }

    public void setDefinition(ReferenceTypeDefinitionPersist definition) {
        this.definition = definition;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(ReferenceTypePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferenceTypePersistValidator extends BaseValidator<ReferenceTypePersist> {

        public static final String ValidatorName = "ReferenceTypePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected ReferenceTypePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ReferenceTypePersist> modelClass() {
            return ReferenceTypePersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferenceTypePersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(ReferenceTypePersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypePersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(ReferenceTypePersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(ReferenceTypePersist._name).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypePersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getName()))
                            .must(() -> this.lessEqualLength(item.getName(), ReferenceTypeEntity._nameLength))
                            .failOn(ReferenceTypePersist._name).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferenceTypePersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(ReferenceTypePersist._code).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), ReferenceTypeEntity._codeLength))
                            .failOn(ReferenceTypePersist._code).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferenceTypePersist._code}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(ReferenceTypePersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferenceTypePersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(ReferenceTypePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(ReferenceTypeDefinitionPersist.ReferenceTypeDefinitionPersistValidator.class))
            );
        }
    }

}
