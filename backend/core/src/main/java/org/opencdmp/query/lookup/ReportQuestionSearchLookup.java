package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.ReportSubjectType;

/**
 * A search over the questions of every asset template and plan blueprint of the tenant, so
 * a report can be started from the question rather than from the template it lives in.
 */
public class ReportQuestionSearchLookup {

    @Schema(description = "Text to match against the numbering, title and field labels of a question, or the name of a semantic. Leave empty to list the semantics in use")
    private String like;

    @Schema(description = "Restrict to asset template or plan blueprint questions. Both when left out")
    private ReportSubjectType subject;

    @Schema(description = "Most question hits and most semantic hits to return, each. Defaults to 30")
    private Integer limit;

    @Schema(description = "Search the templates and blueprints of every tenant instead of the current one. Requires the installation report permission")
    private Boolean installationWide;

    @Schema(description = "Also return the template and blueprint lineages whose name matches the text, for browsing them. Defaults to false")
    private Boolean includeSources;

    public String getLike() { return this.like; }

    public void setLike(String like) { this.like = like; }

    public ReportSubjectType getSubject() { return this.subject; }

    public void setSubject(ReportSubjectType subject) { this.subject = subject; }

    public Integer getLimit() { return this.limit; }

    public void setLimit(Integer limit) { this.limit = limit; }

    public Boolean getInstallationWide() { return this.installationWide; }

    public void setInstallationWide(Boolean installationWide) { this.installationWide = installationWide; }

    public Boolean getIncludeSources() { return this.includeSources; }

    public void setIncludeSources(Boolean includeSources) { this.includeSources = includeSources; }
}
