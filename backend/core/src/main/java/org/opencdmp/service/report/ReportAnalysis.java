package org.opencdmp.service.report;

import org.opencdmp.commons.enums.ReportDateBucket;
import org.opencdmp.commons.enums.ReportSubjectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Everything one report request read from the database, before it is either aggregated
 * into a {@link org.opencdmp.model.report.Report} or written out row by row as CSV. Both
 * outputs answer the same question, so they are produced from one pass over the data.
 */
class ReportAnalysis {

    private ReportSubjectType subject;
    private ReportDefinitionResolver.ParsedSource source;
    private List<ReportQuestionSpec> specs;
    private ReportDefinitionResolver.ParsedSource planSource;
    private List<ReportQuestionSpec> planSpecs;
    private List<ReportDimensionSpec> dimensions;
    private List<ReportSubject> subjects;
    private Map<UUID, Map<String, ReportValueExtractor.Extraction>> extractions;
    private Map<UUID, Map<String, ReportValueExtractor.Extraction>> planExtractions;
    private Map<UUID, String> descriptionStatusNames;
    private Map<UUID, String> planStatusNames;
    private Map<UUID, String> blueprintLabels;
    private ReportDateBucket dateBucket;
    private int textTopN;
    private boolean installationWide;
    private Map<UUID, String> tenantCodes;

    public ReportSubjectType getSubject() { return this.subject; }

    public void setSubject(ReportSubjectType subject) { this.subject = subject; }

    public ReportDefinitionResolver.ParsedSource getSource() { return this.source; }

    public void setSource(ReportDefinitionResolver.ParsedSource source) { this.source = source; }

    public List<ReportQuestionSpec> getSpecs() { return this.specs; }

    public void setSpecs(List<ReportQuestionSpec> specs) { this.specs = specs; }

    public ReportDefinitionResolver.ParsedSource getPlanSource() { return this.planSource; }

    public void setPlanSource(ReportDefinitionResolver.ParsedSource planSource) { this.planSource = planSource; }

    public List<ReportQuestionSpec> getPlanSpecs() { return this.planSpecs; }

    public void setPlanSpecs(List<ReportQuestionSpec> planSpecs) { this.planSpecs = planSpecs; }

    /** Answers of each parent plan, keyed by plan id — read once per plan, not per asset. */
    public Map<UUID, Map<String, ReportValueExtractor.Extraction>> getPlanExtractions() { return this.planExtractions; }

    public void setPlanExtractions(Map<UUID, Map<String, ReportValueExtractor.Extraction>> planExtractions) { this.planExtractions = planExtractions; }

    public List<ReportDimensionSpec> getDimensions() { return this.dimensions; }

    public void setDimensions(List<ReportDimensionSpec> dimensions) { this.dimensions = dimensions; }

    public List<ReportSubject> getSubjects() { return this.subjects; }

    public void setSubjects(List<ReportSubject> subjects) { this.subjects = subjects; }

    public Map<UUID, Map<String, ReportValueExtractor.Extraction>> getExtractions() { return this.extractions; }

    public void setExtractions(Map<UUID, Map<String, ReportValueExtractor.Extraction>> extractions) { this.extractions = extractions; }

    public Map<UUID, String> getDescriptionStatusNames() { return this.descriptionStatusNames; }

    public void setDescriptionStatusNames(Map<UUID, String> descriptionStatusNames) { this.descriptionStatusNames = descriptionStatusNames; }

    public Map<UUID, String> getPlanStatusNames() { return this.planStatusNames; }

    public void setPlanStatusNames(Map<UUID, String> planStatusNames) { this.planStatusNames = planStatusNames; }

    public Map<UUID, String> getBlueprintLabels() { return this.blueprintLabels; }

    public void setBlueprintLabels(Map<UUID, String> blueprintLabels) { this.blueprintLabels = blueprintLabels; }

    public ReportDateBucket getDateBucket() { return this.dateBucket; }

    public void setDateBucket(ReportDateBucket dateBucket) { this.dateBucket = dateBucket; }

    public int getTextTopN() { return this.textTopN; }

    public void setTextTopN(int textTopN) { this.textTopN = textTopN; }

    public boolean isInstallationWide() { return this.installationWide; }

    public void setInstallationWide(boolean installationWide) { this.installationWide = installationWide; }

    /** Tenant codes by tenant id; the default tenant, whose rows carry no tenant, is under the null key. */
    public Map<UUID, String> getTenantCodes() { return this.tenantCodes; }

    public void setTenantCodes(Map<UUID, String> tenantCodes) { this.tenantCodes = tenantCodes; }
}
