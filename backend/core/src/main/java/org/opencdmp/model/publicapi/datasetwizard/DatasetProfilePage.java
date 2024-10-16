package org.opencdmp.model.publicapi.datasetwizard;

import java.util.List;

public class DatasetProfilePage {
    private Integer ordinal;
    private String title;
    private List<Section> sections;

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}
