package org.opencdmp.model.publicapi.datasetwizard;

import java.util.Map;

public interface PropertiesModelBuilder {
    void fromJsonObject(Map<String, Object> properties);

    void fromJsonObject(Map<String, Object> properties, String pathKey);
}
