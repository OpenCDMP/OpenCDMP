package org.opencdmp.model.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallationNotice {

    private String title;
    public static final String _title = "title";

    private String body;
    public static final String _body = "body";

    private String link;
    public static final String _link = "link";

    private String fromVersion;
    public static final String _fromVersion = "fromVersion";

    private String beforeVersion;
    public static final String _beforeVersion = "beforeVersion";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public String getBeforeVersion() {
        return beforeVersion;
    }

    public void setBeforeVersion(String beforeVersion) {
        this.beforeVersion = beforeVersion;
    }
}
