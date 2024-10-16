package org.opencdmp.interceptors.user;

import gr.cite.tools.cache.CacheService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.event.UserCredentialTouchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class UserInterceptorCacheService extends CacheService<UserInterceptorCacheService.UserInterceptorCacheValue> {

    private final ConventionService conventionService;

    public static class UserInterceptorCacheValue {

        public UserInterceptorCacheValue() {
        }

        public UserInterceptorCacheValue(String subjectId, UUID userId) {
            this.subjectId = subjectId;
            this.userId = userId;
        }


        public String getSubjectId() {
            return this.subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }

        private String subjectId;
        private UUID userId;
        private List<String> roles;
        private String providerEmail;
        private List<String> externalProviderNames;

        public UUID getUserId() {
            return this.userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public List<String> getRoles() {
            return this.roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public String getProviderEmail() {
            return this.providerEmail;
        }

        public void setProviderEmail(String providerEmail) {
            this.providerEmail = providerEmail;
        }

        public List<String> getExternalProviderNames() {
            return this.externalProviderNames;
        }

        public void setExternalProviderNames(List<String> externalProviderNames) {
            this.externalProviderNames = externalProviderNames;
        }
    }

    @EventListener
    public void handleTenantTouchedEvent(UserCredentialTouchedEvent event) {
        if (!this.conventionService.isNullOrEmpty(event.getSubjectId()))
            this.evict(this.buildKey(event.getSubjectId()));
    }


    @Autowired
    public UserInterceptorCacheService(UserInterceptorCacheOptions options, ConventionService conventionService) {
        super(options);
        this.conventionService = conventionService;
    }

    @Override
    protected Class<UserInterceptorCacheValue> valueClass() {
        return UserInterceptorCacheValue.class;
    }

    @Override
    public String keyOf(UserInterceptorCacheValue value) {
        return this.buildKey(value.getSubjectId());
    }


    public String buildKey(String subject) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$subject$", subject);
        return this.generateKey(keyParts);
    }
}
