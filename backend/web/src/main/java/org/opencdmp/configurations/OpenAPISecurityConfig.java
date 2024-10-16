package org.opencdmp.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
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
    public OpenAPI openAPI() {
        Components oauthComponent = new Components();
        oauthComponent.addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme());
        OpenAPI openAPI = new OpenAPI();
        openAPI.components(oauthComponent);
        openAPI.addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME));
        return openAPI;
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
                .authorizationUrl(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth")
                .scopes(new Scopes().addString("read_access", "read data")
                        .addString("write_access", "modify data"));
    }

}
