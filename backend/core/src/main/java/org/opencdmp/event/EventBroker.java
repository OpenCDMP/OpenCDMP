package org.opencdmp.event;

import gr.cite.commons.web.oidc.apikey.events.ApiKeyStaleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventBroker {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void emitApiKeyStale(String apiKey) {
        this.applicationEventPublisher.publishEvent(new ApiKeyStaleEvent(apiKey));
    }

    public void emit(ApiKeyStaleEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }
    public void emit(TenantConfigurationTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(TenantTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(UserTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(UserAddedToTenantEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(UserRemovedFromTenantEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(DescriptionTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(DescriptionTemplateTypeTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(EntityDoiTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(PlanTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(TagTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

    public void emit(UserCredentialTouchedEvent event) {
        this.applicationEventPublisher.publishEvent(event);
    }

}
