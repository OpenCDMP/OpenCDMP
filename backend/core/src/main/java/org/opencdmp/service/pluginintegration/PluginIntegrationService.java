package org.opencdmp.service.pluginintegration;

import org.opencdmp.model.pluginintegration.PluginConnectionTestRequest;
import org.opencdmp.model.pluginintegration.PluginConnectionTestResult;
import org.opencdmp.model.pluginintegration.PluginSourceOverview;

import java.util.List;

/** The tenant administrator's view of the deposit, evaluator and file transformer plugins. */
public interface PluginIntegrationService {

    /** Every source of every kind, including installation sources the tenant has switched off. */
    List<PluginSourceOverview> getSources();

    PluginConnectionTestResult testConnection(PluginConnectionTestRequest request);
}
