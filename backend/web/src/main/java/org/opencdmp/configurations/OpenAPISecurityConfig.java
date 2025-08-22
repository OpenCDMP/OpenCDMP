package org.opencdmp.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPISecurityConfig {

    @Value("${keycloak-client.serverUrl}")
    String authServerUrl;
    @Value("${keycloak-client.realm}")
    String realm;

    private static final String OAUTH_SCHEME_NAME = "oAuth_security_schema";

    @Bean
    public OpenApiCustomizer securityCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }
            components.addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme());
            openApi.addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME));
        };
    }

    private SecurityScheme createOAuthScheme() {
        OAuthFlows flows = createOAuthFlows();
        return new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(flows);
    }

    private OAuthFlows createOAuthFlows() {
        OAuthFlow flow = createAuthorizationCodeFlow();
        return new OAuthFlows().implicit(flow);
    }

    private OAuthFlow createAuthorizationCodeFlow() {
        return new OAuthFlow()
                .authorizationUrl(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth");
    }

}
