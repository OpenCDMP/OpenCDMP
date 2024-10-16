package org.opencdmp.service.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ConfigurationProperties(prefix = "metrics.task")
public class UpdateMetricsTaskProperties {

    private boolean enable;

    private int intervalSeconds;

	private Instant nexusDate;

    private String usersLoginClient;

    private ReferenceTypesProperties referenceTypes;

    public boolean getEnable() {
        return enable;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public Instant getNexusDate() {
        return nexusDate;
    }

    public String getUsersLoginClient() {
        return usersLoginClient;
    }

    public ReferenceTypesProperties getReferenceTypes() {
        return referenceTypes;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public void setNexusDate(Instant nexusDate) {
        this.nexusDate = nexusDate;
    }

    public void setUsersLoginClient(String usersLoginClient) {
        this.usersLoginClient = usersLoginClient;
    }

    public void setReferenceTypes(ReferenceTypesProperties referenceTypes) {
        this.referenceTypes = referenceTypes;
    }

    public static class ReferenceTypesProperties {

        private List<UUID> funderIds;

        private List<UUID> grantIds;

        private List<UUID> projectIds;

        private List<UUID> researcherIds;

        public List<UUID> getFunderIds() {
            return funderIds;
        }

        public List<UUID> getGrantIds() {
            return grantIds;
        }

        public List<UUID> getProjectIds() {
            return projectIds;
        }

        public List<UUID> getResearcherIds() {
            return researcherIds;
        }

        public void setFunderIds(List<UUID> funderIds) {
            this.funderIds = funderIds;
        }

        public void setGrantIds(List<UUID> grantIds) {
            this.grantIds = grantIds;
        }

        public void setProjectIds(List<UUID> projectIds) {
            this.projectIds = projectIds;
        }

        public void setResearcherIds(List<UUID> researcherIds) {
            this.researcherIds = researcherIds;
        }
    }

}

