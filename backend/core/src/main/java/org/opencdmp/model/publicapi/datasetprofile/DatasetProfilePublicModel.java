package org.opencdmp.model.publicapi.datasetprofile;

import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;

import java.util.UUID;

public class DatasetProfilePublicModel {
    private UUID id;
    private String label;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public static DatasetProfilePublicModel fromDataModel(DescriptionTemplate entity) {
        DatasetProfilePublicModel model = new DatasetProfilePublicModel();
        model.setId(entity.getId());
        model.setLabel(entity.getLabel());
        return model;
    }

}
