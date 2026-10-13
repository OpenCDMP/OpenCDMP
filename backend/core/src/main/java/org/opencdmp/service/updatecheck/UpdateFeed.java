package org.opencdmp.service.updatecheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.opencdmp.model.maintenance.InstallationNotice;

import java.util.List;

// Fields added to the feed later must not break installations already released.
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateFeed {

    private String latestVersion;
    private String releaseUrl;
    private List<InstallationNotice> notices;

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getReleaseUrl() {
        return releaseUrl;
    }

    public void setReleaseUrl(String releaseUrl) {
        this.releaseUrl = releaseUrl;
    }

    public List<InstallationNotice> getNotices() {
        return notices;
    }

    public void setNotices(List<InstallationNotice> notices) {
        this.notices = notices;
    }
}
