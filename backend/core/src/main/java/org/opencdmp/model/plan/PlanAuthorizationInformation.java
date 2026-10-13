package org.opencdmp.model.plan;

import java.util.List;

public class PlanAuthorizationInformation {

    private List<String> globalAuthorizationFlags;
    public static final String _globalAuthorizationFlags = "globalAuthorizationFlags";

    private List<PlanAuthorizationInformationPerSection> authorizationInformationPerSections;
    public static final String _authorizationInformationPerSections = "authorizationInformationPerSections";

    public static class PlanAuthorizationInformationPerSection {

        private String sectionId;
        public static final String _sectionId = "sectionId";

        private List<String> authorizationFlags;
        public static final String _authorizationFlags = "authorizationFlags";


        public String getSectionId() {
            return sectionId;
        }

        public void setSectionId(String sectionId) {
            this.sectionId = sectionId;
        }

        public List<String> getAuthorizationFlags() {
            return authorizationFlags;
        }

        public void setAuthorizationFlags(List<String> authorizationFlags) {
            this.authorizationFlags = authorizationFlags;
        }
    }

    public List<String> getGlobalAuthorizationFlags() {
        return globalAuthorizationFlags;
    }

    public void setGlobalAuthorizationFlags(List<String> globalAuthorizationFlags) {
        this.globalAuthorizationFlags = globalAuthorizationFlags;
    }

    public List<PlanAuthorizationInformationPerSection> getAuthorizationInformationPerSections() {
        return authorizationInformationPerSections;
    }

    public void setAuthorizationInformationPerSections(List<PlanAuthorizationInformationPerSection> authorizationInformationPerSections) {
        this.authorizationInformationPerSections = authorizationInformationPerSections;
    }
}
