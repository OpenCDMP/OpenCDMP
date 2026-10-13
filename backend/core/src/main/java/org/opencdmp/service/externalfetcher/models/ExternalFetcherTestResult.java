package org.opencdmp.service.externalfetcher.models;

import java.util.List;
import java.util.Map;

public class ExternalFetcherTestResult {

    public final static String _url = "url";
    private String url;

    public final static String _status = "status";
    private Integer status;

    public final static String _error = "error";
    private String error;

    public final static String _durationMs = "durationMs";
    private Long durationMs;

    public final static String _body = "body";
    private String body;

    public final static String _bodyTruncated = "bodyTruncated";
    private Boolean bodyTruncated;

    public final static String _items = "items";
    private List<Map<String, Object>> items;

    public final static String _total = "total";
    private Integer total;

    public final static String _results = "results";
    private List<Map<String, String>> results;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getBodyTruncated() {
        return bodyTruncated;
    }

    public void setBodyTruncated(Boolean bodyTruncated) {
        this.bodyTruncated = bodyTruncated;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Map<String, String>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, String>> results) {
        this.results = results;
    }
}
