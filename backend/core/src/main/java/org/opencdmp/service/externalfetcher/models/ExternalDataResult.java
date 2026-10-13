package org.opencdmp.service.externalfetcher.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExternalDataResult {
    List<Map<String, String>> results;
    List<Map<String, Object>> rawData;
    Integer total;

    public ExternalDataResult() {
        this.results = new ArrayList<>();
    }

    public ExternalDataResult(List<Map<String, String>> results) {
        this.results = results;
    }

    public List<Map<String, String>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, String>> results) {
        this.results = results;
    }

    public List<Map<String, Object>> getRawData() {
        return rawData;
    }

    public void setRawData(List<Map<String, Object>> rawData) {
        this.rawData = rawData;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public void addAll(ExternalDataResult other){
        if (other == null) return;
        if (other.getResults() != null){
            if (this.getResults() == null) this.setResults(new ArrayList<>());
            this.getResults().addAll(other.getResults());
        }
        if (other.getRawData() != null){
            if (this.getRawData() == null) this.setRawData(new ArrayList<>());
            this.getRawData().addAll(other.getRawData());
        }
        if (other.getTotal() != null) {
            this.setTotal((this.getTotal() == null ? 0 : this.getTotal()) + other.getTotal());
        }
    }
}
