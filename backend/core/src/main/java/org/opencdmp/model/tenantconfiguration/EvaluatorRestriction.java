package org.opencdmp.model.tenantconfiguration;

import java.util.List;

public class EvaluatorRestriction {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";

    private List<String> benchmarkIds;
    public static final String _benchmarkIds = "benchmarkIds";

    private List<String> testIds;
    public static final String _testIds = "testIds";

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public List<String> getBenchmarkIds() {
        return benchmarkIds;
    }

    public void setBenchmarkIds(List<String> benchmarkIds) {
        this.benchmarkIds = benchmarkIds;
    }

    public List<String> getTestIds() {
        return testIds;
    }

    public void setTestIds(List<String> testIds) {
        this.testIds = testIds;
    }
}
