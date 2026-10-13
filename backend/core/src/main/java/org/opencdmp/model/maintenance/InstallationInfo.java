package org.opencdmp.model.maintenance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InstallationInfo {

    private UUID id;
    public static final String _id = "id";

    private String version;
    public static final String _version = "version";

    private Boolean updateCheckEnabled;
    public static final String _updateCheckEnabled = "updateCheckEnabled";

    private Boolean shareUsage;
    public static final String _shareUsage = "shareUsage";

    private Instant checkedAt;
    public static final String _checkedAt = "checkedAt";

    private String latestVersion;
    public static final String _latestVersion = "latestVersion";

    private String releaseUrl;
    public static final String _releaseUrl = "releaseUrl";

    private Boolean updateAvailable;
    public static final String _updateAvailable = "updateAvailable";

    private List<InstallationNotice> notices;
    public static final String _notices = "notices";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getUpdateCheckEnabled() {
        return updateCheckEnabled;
    }

    public void setUpdateCheckEnabled(Boolean updateCheckEnabled) {
        this.updateCheckEnabled = updateCheckEnabled;
    }

    public Boolean getShareUsage() {
        return shareUsage;
    }

    public void setShareUsage(Boolean shareUsage) {
        this.shareUsage = shareUsage;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(Instant checkedAt) {
        this.checkedAt = checkedAt;
    }

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

    public Boolean getUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(Boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    public List<InstallationNotice> getNotices() {
        return notices;
    }

    public void setNotices(List<InstallationNotice> notices) {
        this.notices = notices;
    }
}
