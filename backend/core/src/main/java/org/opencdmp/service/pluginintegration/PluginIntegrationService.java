package org.opencdmp.service.pluginintegration;

import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.model.pluginintegration.PluginConnectionTestRequest;
import org.opencdmp.model.pluginintegration.PluginConnectionTestResult;
import org.opencdmp.model.pluginintegration.PluginSourceOverview;

import java.util.List;
import java.util.Set;

/** The tenant administrator's view of the deposit, evaluator and file transformer plugins. */
public interface PluginIntegrationService {

    /** Every source of every kind, including installation sources the tenant has switched off. */
    List<PluginSourceOverview> getSources();

    PluginConnectionTestResult testConnection(PluginConnectionTestRequest request);

    /** Codes of the settings the plugin marks as credentials, or null when the plugin could not be asked. */
    Set<String> secretFieldCodes(PluginType pluginType, String pluginCode);
}
