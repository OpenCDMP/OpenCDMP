package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.types.descriptiontemplate.RuleEntity;
import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class VisibilityEntity {
    @XmlElementWrapper(name = "rules")
    @XmlElement(name = "rule")
    private List<RuleEntity> rules;
    @XmlAttribute(name="style")
    private String style;

    public List<RuleEntity> getRules() {
        return rules;
    }

    public void setRules(List<RuleEntity> rules) {
        this.rules = rules;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
