package org.opencdmp.controllers;

import gr.cite.commons.web.authz.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Hidden;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.locale.LocaleProperties;
import org.opencdmp.model.tenantconfiguration.DefaultUserLocaleTenantConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * What applies when a tenant stores no value of its own, so the settings pages can show it instead of an empty
 * field. These come from the installation's configuration files, not from any tenant configuration record.
 */
@RestController
@RequestMapping(path = "api/tenant-configuration-defaults")
@Hidden
public class TenantConfigurationDefaultsController {

    private final AuthorizationService authorizationService;
    private final LocaleProperties localeProperties;

    public TenantConfigurationDefaultsController(AuthorizationService authorizationService, LocaleProperties localeProperties) {
        this.authorizationService = authorizationService;
        this.localeProperties = localeProperties;
    }

    @GetMapping("default-user-locale")
    public DefaultUserLocaleTenantConfiguration getDefaultUserLocale() {
        this.authorizationService.authorizeForce(Permission.BrowseTenantConfiguration);

        DefaultUserLocaleTenantConfiguration model = new DefaultUserLocaleTenantConfiguration();
        model.setTimezone(this.localeProperties.getTimezone());
        model.setLanguage(this.localeProperties.getLanguage());
        model.setCulture(this.localeProperties.getCulture());
        return model;
    }
}
