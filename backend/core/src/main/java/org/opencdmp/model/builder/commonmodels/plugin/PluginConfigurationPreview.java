package org.opencdmp.model.builder.commonmodels.plugin;

import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;

import java.util.List;
import java.util.function.Supplier;

/**
 * Plugin configuration that stands in for the tenant's stored one while an export is built, so the settings
 * pages can preview unsaved changes through the normal export path. Held per thread and only for the duration
 * of {@link #with}; outside it the stored tenant configuration applies as usual.
 */
public final class PluginConfigurationPreview {

    private static final ThreadLocal<List<PluginConfigurationEntity>> TENANT_OVERRIDE = new ThreadLocal<>();

    private PluginConfigurationPreview() {
    }

    public static <T> T with(List<PluginConfigurationEntity> tenantConfigurations, Supplier<T> action) {
        List<PluginConfigurationEntity> previous = TENANT_OVERRIDE.get();
        TENANT_OVERRIDE.set(tenantConfigurations);
        try {
            return action.get();
        } finally {
            if (previous == null) TENANT_OVERRIDE.remove();
            else TENANT_OVERRIDE.set(previous);
        }
    }

    /** The configuration to use instead of the stored tenant one, or {@code null} outside a preview. */
    public static List<PluginConfigurationEntity> tenantOverride() {
        return TENANT_OVERRIDE.get();
    }
}
