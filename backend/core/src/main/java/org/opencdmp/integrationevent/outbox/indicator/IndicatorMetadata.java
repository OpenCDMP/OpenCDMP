package org.opencdmp.integrationevent.outbox.indicator;

import java.time.Instant;

public class IndicatorMetadata {

    private String label;

    private String description;

    private String url;

    private String code;

//    private List<SemanticsLabelEvent> semanticLabels;
//
//    private List<AltTextPersist> altLabels;
//
//    private List<AltTextPersist> altDescriptions;

    private Instant date;

//    private List<CoveragePersist> coverage;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

//    public List<SemanticsLabelPersist> getSemanticLabels() {
//        return semanticLabels;
//    }
//
//    public void setSemanticLabels(List<SemanticsLabelPersist> semanticLabels) {
//        this.semanticLabels = semanticLabels;
//    }
//
//    public List<AltTextPersist> getAltLabels() {
//        return altLabels;
//    }
//
//    public void setAltLabels(List<AltTextPersist> altLabels) {
//        this.altLabels = altLabels;
//    }
//
//    public List<AltTextPersist> getAltDescriptions() {
//        return altDescriptions;
//    }
//
//    public void setAltDescriptions(List<AltTextPersist> altDescriptions) {
//        this.altDescriptions = altDescriptions;
//    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

//    public List<CoveragePersist> getCoverage() {
//        return coverage;
//    }
//
//    public void setCoverage(List<CoveragePersist> coverage) {
//        this.coverage = coverage;
//    }
}
