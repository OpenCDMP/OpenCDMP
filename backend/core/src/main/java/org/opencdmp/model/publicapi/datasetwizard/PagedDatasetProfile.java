package org.opencdmp.model.publicapi.datasetwizard;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PagedDatasetProfile {
    private List<DatasetProfilePage> pages;
    private List<Rule> rules;
    private int status;

    public List<DatasetProfilePage> getPages() {
        return pages;
    }

    public void setPages(List<DatasetProfilePage> pages) {
        this.pages = pages;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PagedDatasetProfile buildPagedDatasetProfile(DatasetProfile profile) {
        this.status = profile.getStatus();
        this.rules = profile.getRules();
        this.pages = new LinkedList<>();
        List<Page> pages = profile.getPages();
        for (Page page : pages) {
            DatasetProfilePage datasetProfilePage = new DatasetProfilePage();
            datasetProfilePage.setOrdinal(page.getOrdinal());
            datasetProfilePage.setTitle(page.getTitle());
            datasetProfilePage.setSections(profile.getSections().stream().filter(item -> item.getPage().equals(page.getId())).collect(Collectors.toList()));
            this.pages.add(datasetProfilePage);
        }

        return this;
    }

    public void toMap(Map<String, Object> fieldValues) {
        this.pages.forEach(item -> item.getSections().forEach(sectionItem -> sectionItem.toMap(fieldValues)));
    }

    public void toMap(Map<String, Object> fieldValues, int index) {

    }
}
