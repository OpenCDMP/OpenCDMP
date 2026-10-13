package org.opencdmp.model.persist;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.WebhookEventType;
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

public class WebhookSubscriptionPersist {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private String url;
    public static final String _url = "url";

    private List<WebhookEventType> eventTypes;
    public static final String _eventTypes = "eventTypes";

    private Boolean enabled;
    public static final String _enabled = "enabled";

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<WebhookEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<WebhookEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(WebhookSubscriptionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class WebhookSubscriptionPersistValidator extends BaseValidator<WebhookSubscriptionPersist> {

        public static final String ValidatorName = "WebhookSubscriptionPersistValidator";

        private final MessageSource messageSource;

        protected WebhookSubscriptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<WebhookSubscriptionPersist> modelClass() {
            return WebhookSubscriptionPersist.class;
        }

        @Override
        protected List<Specification> specifications(WebhookSubscriptionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> this.isValidGuid(item.getId()))
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(WebhookSubscriptionPersist._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{WebhookSubscriptionPersist._hash}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isValidGuid(item.getId()))
                            .must(() -> !this.isValidHash(item.getHash()))
                            .failOn(WebhookSubscriptionPersist._hash).failWith(this.messageSource.getMessage("Validation_OverPosting", new Object[]{}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(WebhookSubscriptionPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{WebhookSubscriptionPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getUrl()))
                            .failOn(WebhookSubscriptionPersist._url).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{WebhookSubscriptionPersist._url}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getUrl()))
                            .must(() -> item.getUrl().startsWith("https://") || item.getUrl().startsWith("http://"))
                            .failOn(WebhookSubscriptionPersist._url).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{WebhookSubscriptionPersist._url}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getEventTypes()))
                            .failOn(WebhookSubscriptionPersist._eventTypes).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{WebhookSubscriptionPersist._eventTypes}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getEnabled()))
                            .failOn(WebhookSubscriptionPersist._enabled).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{WebhookSubscriptionPersist._enabled}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
