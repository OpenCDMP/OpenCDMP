package org.opencdmp.model.pluginintegration;

public class PluginConnectionTestResult {

    private boolean success;
    /** The id the plugin reports for itself, which should match the id it is registered under. */
    private String reportedId;
    private String message;

    public PluginConnectionTestResult() {
    }

    public PluginConnectionTestResult(boolean success, String reportedId, String message) {
        this.success = success;
        this.reportedId = reportedId;
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReportedId() {
        return reportedId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
