package org.opencdmp.service.keycloak;

import gr.cite.commons.web.keycloak.api.configuration.KeycloakClientConfiguration;
import gr.cite.commons.web.keycloak.api.modules.ClientsModule;
import gr.cite.commons.web.keycloak.api.modules.GroupsModule;
import gr.cite.commons.web.keycloak.api.modules.Modules;
import gr.cite.tools.logging.LoggerService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyKeycloakAdminRestApi {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MyKeycloakAdminRestApi.class));

    private final KeycloakClientConfiguration configuration;

    protected RealmResource realm;

    @Autowired
    public MyKeycloakAdminRestApi(KeycloakClientConfiguration configuration) {
        this.configuration = configuration;
        Keycloak client = this.initClient();
        this.realm = client.realm(configuration.getProperties().getRealm());
        logger.info("Custom Keycloak client initialized. Keycloak serving from {}", configuration.getProperties().getServerUrl().replace("/auth", "").replaceAll("https?://", ""));
    }

    public MyUsersModule users() {
        return MyModules.getUsersModule(this.realm);
    }

    public GroupsModule groups() {
        return Modules.getGroupsModule(this.realm);
    }

    public ClientsModule clients() {
        return Modules.getClientsModule(this.realm);
    }

    private Keycloak initClient() {
        return KeycloakBuilder.builder().serverUrl(this.configuration.getProperties().getServerUrl()).realm(this.configuration.getProperties().getRealm()).username(this.configuration.getProperties().getUsername()).password(this.configuration.getProperties().getPassword()).clientId(this.configuration.getProperties().getClientId()).clientSecret(this.configuration.getProperties().getClientSecret()).grantType("password").build();
    }
}
