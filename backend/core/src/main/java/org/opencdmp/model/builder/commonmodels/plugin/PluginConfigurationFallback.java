package org.opencdmp.model.builder.commonmodels.plugin;

import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves the plugin configuration a blueprint or description template hands to a plugin: its own
 * configuration for that plugin, completed field by field from the tenant's plugin configuration.
 * A field the definition leaves empty — or does not have at all, e.g. a field the plugin added
 * after the definition was saved — takes the tenant's value; a definition without configuration
 * for the plugin gets the tenant's. The entities passed in are never modified.
 */
public final class PluginConfigurationFallback {

    private PluginConfigurationFallback() {
    }

    public static List<PluginConfigurationEntity> resolve(List<PluginConfigurationEntity> definition, List<PluginConfigurationEntity> tenant, String pluginCode) {
        PluginConfigurationEntity own = find(definition, pluginCode);
        PluginConfigurationEntity fallback = find(tenant, pluginCode);
        if (own == null && fallback == null) return List.of();
        if (own == null) return List.of(fallback);
        if (fallback == null || (fallback.getPluginType() != null && own.getPluginType() != null && !fallback.getPluginType().equals(own.getPluginType()))) return List.of(own);

        PluginConfigurationEntity merged = new PluginConfigurationEntity();
        merged.setPluginCode(own.getPluginCode());
        merged.setPluginType(own.getPluginType());
        List<PluginConfigurationFieldEntity> fields = new ArrayList<>();
        List<PluginConfigurationFieldEntity> ownFields = own.getFields() != null ? own.getFields() : List.of();
        List<PluginConfigurationFieldEntity> tenantFields = fallback.getFields() != null ? fallback.getFields() : List.of();
        for (PluginConfigurationFieldEntity field : ownFields) {
            PluginConfigurationFieldEntity tenantField = tenantFields.stream().filter(x -> Objects.equals(x.getCode(), field.getCode())).findFirst().orElse(null);
            fields.add(hasValue(field) || tenantField == null ? field : tenantField);
        }
        for (PluginConfigurationFieldEntity tenantField : tenantFields) {
            if (ownFields.stream().noneMatch(x -> Objects.equals(x.getCode(), tenantField.getCode()))) fields.add(tenantField);
        }
        merged.setFields(fields);
        return List.of(merged);
    }

    private static PluginConfigurationEntity find(List<PluginConfigurationEntity> configurations, String pluginCode) {
        if (configurations == null || pluginCode == null) return null;
        return configurations.stream().filter(x -> pluginCode.equals(x.getPluginCode())).findFirst().orElse(null);
    }

    private static boolean hasValue(PluginConfigurationFieldEntity field) {
        return field.getFileValue() != null || (field.getTextValue() != null && !field.getTextValue().isBlank());
    }
}
