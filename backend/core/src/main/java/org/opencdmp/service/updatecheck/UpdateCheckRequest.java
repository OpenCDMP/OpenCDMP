package org.opencdmp.service.updatecheck;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

// With usage sharing switched off, only the installation id and version are sent.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCheckRequest {

    private UUID installation;
    private String version;
    private String url;
    private UpdateCheckUsage usage;

    public UUID getInstallation() {
        return installation;
    }

    public void setInstallation(UUID installation) {
        this.installation = installation;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UpdateCheckUsage getUsage() {
        return usage;
    }

    public void setUsage(UpdateCheckUsage usage) {
        this.usage = usage;
    }
}
