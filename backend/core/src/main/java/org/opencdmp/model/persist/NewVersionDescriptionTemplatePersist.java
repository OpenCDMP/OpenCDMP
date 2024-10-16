package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
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

public class NewVersionDescriptionTemplatePersist {

    private UUID id = null;

    private String label = null;

    public static final String _label = "label";

    private String description = null;

    public static final String _description = "description";

    private String language = null;

    public static final String _language = "language";

    private UUID type = null;

    public static final String _type = "type";

    private DescriptionTemplateStatus status;

    public static final String _status = "status";

    private DefinitionPersist definition = null;

    public static final String _definition = "definition";

    private List<UserDescriptionTemplatePersist> users = null;

    public static final String _users = "users";

    private String hash;

    public static final String _hash = "hash";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UUID getType() {
        return type;
    }

    public void setType(UUID type) {
        this.type = type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public DescriptionTemplateStatus getStatus() {
        return status;
    }

    public void setStatus(DescriptionTemplateStatus status) {
        this.status = status;
    }

    public DefinitionPersist getDefinition() {
        return definition;
    }

    public void setDefinition(DefinitionPersist definition) {
        this.definition = definition;
    }

    public List<UserDescriptionTemplatePersist> getUsers() {
        return users;
    }

    public void setUsers(List<UserDescriptionTemplatePersist> users) {
        this.users = users;
    }

    @Component(NewVersionDescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class NewVersionDescriptionTemplatePersistValidator extends BaseValidator<NewVersionDescriptionTemplatePersist> {

        public static final String ValidatorName = "NewVersionDescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected NewVersionDescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<NewVersionDescriptionTemplatePersist> modelClass() {
            return NewVersionDescriptionTemplatePersist.class;
        }

        @Override
        protected List<Specification> specifications(NewVersionDescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(NewVersionDescriptionTemplatePersist._hash).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(NewVersionDescriptionTemplatePersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getLabel()))
                            .must(() -> this.lessEqualLength(item.getLabel(), DescriptionTemplateEntity._labelLength))
                            .failOn(NewVersionDescriptionTemplatePersist._label).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{NewVersionDescriptionTemplatePersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getDescription()))
                            .failOn(NewVersionDescriptionTemplatePersist._description).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._description}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLanguage()))
                            .failOn(NewVersionDescriptionTemplatePersist._language).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._language}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getType()))
                            .failOn(NewVersionDescriptionTemplatePersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._type}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStatus()))
                            .failOn(NewVersionDescriptionTemplatePersist._status).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._status}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> !this.isNull(item.getDefinition()))
                            .failOn(NewVersionDescriptionTemplatePersist._definition).failWith(messageSource.getMessage("Validation_Required", new Object[]{NewVersionDescriptionTemplatePersist._definition}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getDefinition()))
                            .on(NewVersionDescriptionTemplatePersist._definition)
                            .over(item.getDefinition())
                            .using(() -> this.validatorFactory.validator(DefinitionPersist.DefinitionPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getUsers()))
                            .on(NewVersionDescriptionTemplatePersist._users)
                            .over(item.getUsers())
                            .using((itm) -> this.validatorFactory.validator(UserDescriptionTemplatePersist.UserDescriptionTemplatePersistValidator.class))
            );
        }
    }

}

