package org.opencdmp.integrationevent.outbox.usertouched;

import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.integrationevent.TrackedEvent;
import org.opencdmp.model.UserContactInfo;

import java.util.List;
import java.util.UUID;

public class UserTouchedIntegrationEvent extends TrackedEvent {

    private UUID id;

    private String name;

    private UserProfile profile;

    private List<UserContactInfo> userContactInfo;
    private List<TenantUser> tenantUsers;
    private List<UserCredential> credentials;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public List<UserContactInfo> getUserContactInfo() {
        return userContactInfo;
    }

    public void setUserContactInfo(List<UserContactInfo> userContactInfo) {
        this.userContactInfo = userContactInfo;
    }

    public List<TenantUser> getTenantUsers() {
        return tenantUsers;
    }

    public void setTenantUsers(List<TenantUser> tenantUsers) {
        this.tenantUsers = tenantUsers;
    }

    public List<UserCredential> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<UserCredential> credentials) {
        this.credentials = credentials;
    }

    public static class UserProfile {

        private String timezone;

        private String culture;

        private String language;

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getCulture() {
            return culture;
        }

        public void setCulture(String culture) {
            this.culture = culture;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    public static class UserContactInfo {

        private ContactInfoType type;

        private String value;
        private int ordinal;

        public ContactInfoType getType() {
            return type;
        }

        public void setType(ContactInfoType type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getOrdinal() {
            return ordinal;
        }

        public void setOrdinal(int ordinal) {
            this.ordinal = ordinal;
        }
    }




    public static class UserCredential {
        private String subjectId;

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }
    }

    public static class TenantUser {

        private UUID tenant;

        public UUID getTenant() {
            return tenant;
        }

        public void setTenant(UUID tenant) {
            this.tenant = tenant;
        }
    }

}
