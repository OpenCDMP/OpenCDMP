package org.opencdmp.controllers;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import io.swagger.v3.oas.annotations.Hidden;
import org.opencdmp.model.pluginintegration.PluginConnectionTestRequest;
import org.opencdmp.model.pluginintegration.PluginConnectionTestResult;
import org.opencdmp.model.pluginintegration.PluginSourceOverview;
import org.opencdmp.service.pluginintegration.PluginIntegrationService;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Backs the Integrations screens of the tenant configuration: which plugins exist, which are on, and whether they answer. */
@RestController
@RequestMapping(path = "api/plugin-integration")
@Hidden
public class PluginIntegrationController {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PluginIntegrationController.class));

    private final PluginIntegrationService pluginIntegrationService;

    public PluginIntegrationController(PluginIntegrationService pluginIntegrationService) {
        this.pluginIntegrationService = pluginIntegrationService;
    }

    @GetMapping("sources")
    public List<PluginSourceOverview> getSources() {
        logger.debug(new MapLogEntry("retrieving plugin sources"));
        return this.pluginIntegrationService.getSources();
    }

    @PostMapping("test")
    public PluginConnectionTestResult test(@RequestBody PluginConnectionTestRequest request) {
        logger.debug(new MapLogEntry("testing plugin connection").And("type", request != null ? request.getPluginType() : null).And("id", request != null ? request.getId() : null));
        return this.pluginIntegrationService.testConnection(request);
    }
}
