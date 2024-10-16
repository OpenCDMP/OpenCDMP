package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptiontemplatedefinition.DefinitionPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DescriptionTemplatePersist {

    private UUID id;

    private String label;

    public static final String _label = "label";

    private String code;

    public final static String _code = "code";


    private String description;

    public static final String _description = "description";

    private String language;

    public static final String _language = "language";

    private UUID type;

    public static final String _type = "type";

    private DescriptionTemplateStatus status;

    public static final String _status = "status";

    private DefinitionPersist definition;

    public static final String _definition = "definition";

    private List<UserDescriptionTemplatePersist> users;

    public static final String _users = "users";

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UUID getType() {
        return this.type;
    }

    public void setType(UUID type) {
        this.type = type;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public DescriptionTemplateStatus getStatus() {
        return this.status;
    }

    public void setStatus(DescriptionTemplateStatus status) {
        this.status = status;
    }

    public DefinitionPersist getDefinition() {
        return this.definition;
    }

    public void setDefinition(DefinitionPersist definition) {
        this.definition = definition;
    }

    public List<UserDescriptionTemplatePersist> getUsers() {
        return this.users;
    }

    public void setUsers(List<UserDescriptionTemplatePersist> users) {
        this.users = users;
    }

    @Component(DescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionTemplatePersistValidator extends BaseValidator<DescriptionTemplatePersist> {

        public static final String ValidatorName = "DescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected DescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionTemplatePersist> modelClass() {
            return DescriptionTemplatePersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(DescriptionTemplatePersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(DescriptionTemplatePersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(DescriptionTemplatePersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), DescriptionTemplateEntity._labelLength))
                            .failOn(DescriptionTemplatePersist._label).failWith(this.messageSource.getMessage("Validation_MaxLength", new Object[]{DescriptionTemplatePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(DescriptionTemplatePersist._code).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getCode()))
                            .must(() -> this.lessEqualLength(item.getCode(), DescriptionTemplateTypeEntity._codeLength))
                            .failOn(DescriptionTemplatePersist._code).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{DescriptionTemplatePersist._code}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getStatus() == DescriptionTemplateStatus.Finalized)
                            .must(() -> !this.isEmpty(item.getDescription()))
                            .failOn(DescriptionTemplatePersist._description).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._description}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getStatus() == DescriptionTemplateStatus.Finalized)
                            .must(() -> !this.isEmpty(item.getLanguage()))
                            .failOn(DescriptionTemplatePersist._language).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._language}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> item.getStatus() == DescriptionTemplateStatus.Finalized)
                            .must(() -> this.isValidGuid(item.getType()))
                            .failOn(DescriptionTemplatePersist._type).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._type}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(DescriptionTemplatePersist._status).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._status}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .iff(() -> item.getStatus() == DescriptionTemplateStatus.Finalized)
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(DescriptionTemplatePersist._definition).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()) && !this.isListNullOrEmpty(item.getDefinition().getPages()))
                            .on(DescriptionTemplatePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> item.getStatus() == DescriptionTemplateStatus.Finalized)
                            .on(DescriptionTemplatePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .on(DescriptionTemplatePersist._users)
                            .over(item.getUsers())
                            .using((itm) -> this.validatorFactory.validator(UserDescriptionTemplatePersist.UserDescriptionTemplatePersistValidator.class))
            );
        }
    }

}


