package org.opencdmp.service.report;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "report")
public class ReportProperties {

    private int maxSubjects;
    private int maxSubjectsInstallation;
    private int maxQuestions;
    private int maxDimensions;
    private int textTopN;
    private int searchCacheSeconds;

    public int getMaxSubjects() {
        return maxSubjects;
    }

    public void setMaxSubjects(int maxSubjects) {
        this.maxSubjects = maxSubjects;
    }

    public int getMaxSubjectsInstallation() {
        return maxSubjectsInstallation;
    }

    public void setMaxSubjectsInstallation(int maxSubjectsInstallation) {
        this.maxSubjectsInstallation = maxSubjectsInstallation;
    }

    public int getMaxQuestions() {
        return maxQuestions;
    }

    public void setMaxQuestions(int maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public int getMaxDimensions() {
        return maxDimensions;
    }

    public void setMaxDimensions(int maxDimensions) {
        this.maxDimensions = maxDimensions;
    }

    public int getTextTopN() {
        return textTopN;
    }

    public void setTextTopN(int textTopN) {
        this.textTopN = textTopN;
    }

    public int getSearchCacheSeconds() {
        return searchCacheSeconds;
    }

    public void setSearchCacheSeconds(int searchCacheSeconds) {
        this.searchCacheSeconds = searchCacheSeconds;
    }
}
