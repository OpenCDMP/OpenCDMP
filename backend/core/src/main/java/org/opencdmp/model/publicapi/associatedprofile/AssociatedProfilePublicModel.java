package org.opencdmp.model.publicapi.associatedprofile;

import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.PlanDescriptionTemplate;
import jakarta.xml.bind.annotation.*;

import java.util.*;

@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class AssociatedProfilePublicModel  {
    @XmlAttribute(name="profileId")
    private UUID id;
    @XmlAttribute(name="descriptionTemplateId")
    private UUID descriptionTemplateId;
    @XmlAttribute(name="label")
    private String label;
    @XmlElement(name="data")
    private Map<String, Object> data;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDescriptionTemplateId() {
        return descriptionTemplateId;
    }

    public void setDescriptionTemplateId(UUID descriptionTemplateId) {
        this.descriptionTemplateId = descriptionTemplateId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public static AssociatedProfilePublicModel fromPlanDescriptionTemplate(PlanDescriptionTemplate planDescriptionTemplate, PlanBlueprint planBlueprint) {
        DescriptionTemplate currentDescriptionTemplate = planDescriptionTemplate.getCurrentDescriptionTemplate();
        UUID sectionId = planDescriptionTemplate.getSectionId();

        AssociatedProfilePublicModel model = new AssociatedProfilePublicModel();
        model.setId(planDescriptionTemplate.getId());
        model.setDescriptionTemplateId(currentDescriptionTemplate.getId());
        model.setLabel(currentDescriptionTemplate.getLabel());
        HashMap<String, Object> data = new HashMap<>();
        data.put("planSectionIndex", getPlanSectionIndexes(planBlueprint, sectionId));
        model.setData(data);

        return model;
    }

    private static List<Integer> getPlanSectionIndexes(PlanBlueprint planBlueprint, UUID sectionId) {
        ArrayList<Integer> indexes = new ArrayList<>();
        planBlueprint.getDefinition().getSections().forEach(x -> {
            if (Objects.equals(sectionId, x.getId()))
                indexes.add(x.getOrdinal());
        });
        return indexes;
    }
}
