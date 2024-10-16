package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.QueryConfig;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

public class QueryConfigEntity implements QueryConfig<QueryCaseConfigEntity> {

    private String name;
    private String defaultValue;
    List<QueryCaseConfigEntity> cases;

    @Override
    public String getName() {
        return name;
    }

    @XmlElement(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @XmlElement(name = "defaultValue")
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public List<QueryCaseConfigEntity> getCases() {
        return cases;
    }

    @XmlElementWrapper
    @XmlElement(name = "case")
    public void setCases(List<QueryCaseConfigEntity> cases) {
        this.cases = cases;
    }
}

