package org.opencdmp.model.externalfetcher;

import java.util.List;

public class QueryConfig {

    public final static String _name = "name";
    private String name;

    public final static String _defaultValue = "defaultValue";

    private String defaultValue;

    public final static String _cases = "cases";
    List<QueryCaseConfig> cases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<QueryCaseConfig> getCases() {
        return cases;
    }

    public void setCases(List<QueryCaseConfig> cases) {
        this.cases = cases;
    }
}
