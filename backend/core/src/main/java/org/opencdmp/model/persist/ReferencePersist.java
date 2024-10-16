package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.referencedefinition.DefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReferencePersist {

    private UUID id;

    private String label;

    public static final String _label = "label";

    private UUID typeId;

    public static final String _typeId = "typeId";

    private String description;

    private DefinitionPersist definition;

    public static final String _definition = "definition";

    private String reference;

    public static final String _reference = "reference";

    private String abbreviation;

    public static final String _abbreviation = "abbreviation";

    private String source;

    public static final String _source = "source";

    private ReferenceSourceType sourceType;

    public static final String _sourceType = "sourceType";

    private String hash;

    public static final String _hash = "hash";

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

    public UUID getTypeId() {
        return this.typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DefinitionPersist getDefinition() {
        return this.definition;
    }

    public void setDefinition(DefinitionPersist definition) {
        this.definition = definition;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ReferenceSourceType getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(ReferenceSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(ReferencePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferencePersistValidator extends BaseValidator<ReferencePersist> {

        public static final String ValidatorName = "ReferencePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected ReferencePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ReferencePersist> modelClass() {
            return ReferencePersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferencePersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(ReferencePersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(ReferencePersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(ReferencePersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), ReferenceEntity._labelLength))
                            .failOn(ReferencePersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getTypeId()))
                            .failOn(ReferencePersist._typeId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._typeId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getReference()))
                            .failOn(ReferencePersist._reference).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._reference}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getReference()))
                            .must(() -> this.lessEqualLength(item.getReference(), ReferenceEntity._referenceLength))
                            .failOn(ReferencePersist._reference).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._reference}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getAbbreviation()))
                            .must(() -> this.lessEqualLength(item.getAbbreviation(), ReferenceEntity._abbreviationLength))
                            .failOn(ReferencePersist._abbreviation).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._abbreviation}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSource()))
                            .failOn(ReferencePersist._source).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._source}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getSource()))
                            .must(() -> this.lessEqualLength(item.getSource(), ReferenceEntity._sourceLength))
                            .failOn(ReferencePersist._source).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._source}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getSourceType()))
                            .failOn(ReferencePersist._sourceType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._sourceType}, LocaleContextHolder.getLocale())),
                   this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(ReferencePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class))
            );
        }
    }

    @Component(ReferenceWithoutTypePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ReferenceWithoutTypePersistValidator extends BaseValidator<ReferencePersist> {

        public static final String ValidatorName = "ReferenceWithoutTypePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected ReferenceWithoutTypePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ReferencePersist> modelClass() {
            return ReferencePersist.class;
        }

        @Override
        protected List<Specification> specifications(ReferencePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(ReferencePersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), ReferenceEntity._labelLength))
                            .failOn(ReferencePersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getReference()))
                            .failOn(ReferencePersist._reference).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._reference}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getReference()))
                            .must(() -> this.lessEqualLength(item.getReference(), ReferenceEntity._referenceLength))
                            .failOn(ReferencePersist._reference).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._reference}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getAbbreviation()))
                            .must(() -> this.lessEqualLength(item.getAbbreviation(), ReferenceEntity._abbreviationLength))
                            .failOn(ReferencePersist._abbreviation).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._abbreviation}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getSource()))
                            .failOn(ReferencePersist._source).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._source}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getSource()))
                            .must(() -> this.lessEqualLength(item.getSource(), ReferenceEntity._sourceLength))
                            .failOn(ReferencePersist._source).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{ReferencePersist._source}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getSourceType()))
                            .failOn(ReferencePersist._sourceType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ReferencePersist._sourceType}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(ReferencePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class))
            );
        }
    }

}
