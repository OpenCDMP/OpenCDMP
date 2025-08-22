package org.opencdmp.configurations;

import io.swagger.v3.oas.models.servers.Server;
import org.opencdmp.configurations.swagger.SwaggerProperties;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiGroupConfig {

    private final SwaggerProperties swaggerProperties;

    public OpenApiGroupConfig(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    public GroupedOpenApi legacyApi( @Qualifier("securityCustomizer") OpenApiCustomizer securityCustomizer,
                                     @Qualifier("sortOperations") OpenApiCustomizer sortOperations,
                                     @Qualifier("sortTags") OpenApiCustomizer sortTags,
                                     @Qualifier("serverUrlCustomizer") OpenApiCustomizer serverUrlCustomizer
    ) {
        if (this.swaggerProperties.getGroup().getLegacyApi() != null) {
            return GroupedOpenApi.builder()
                    .group(this.swaggerProperties.getGroup().getLegacyApi().getGroup())
                    .displayName(this.swaggerProperties.getGroup().getLegacyApi().getDisplayName())
                    .packagesToScan(this.swaggerProperties.getGroup().getLegacyApi().getPackagesToScan())
                    .pathsToMatch(this.swaggerProperties.getGroup().getLegacyApi().getPathsToMatch().split(",\\s*"))
                    .addOpenApiCustomizer(securityCustomizer)
                    .addOpenApiCustomizer(sortOperations)
                    .addOpenApiCustomizer(sortTags)
                    .addOpenApiCustomizer(serverUrlCustomizer)
                    .build();
        }
        return null;

    }

    @Bean
    public GroupedOpenApi currentApi( @Qualifier("securityCustomizer") OpenApiCustomizer securityCustomizer,
                                      @Qualifier("sortOperations") OpenApiCustomizer sortOperations,
                                      @Qualifier("sortTags") OpenApiCustomizer sortTags,
                                      @Qualifier("serverUrlCustomizer") OpenApiCustomizer serverUrlCustomizer
    ) {
        if (this.swaggerProperties.getGroup().getCurrentApi() != null) {
            return GroupedOpenApi.builder()
                    .group(this.swaggerProperties.getGroup().getCurrentApi().getGroup())
                    .displayName(this.swaggerProperties.getGroup().getCurrentApi().getDisplayName())
                    .packagesToScan(this.swaggerProperties.getGroup().getCurrentApi().getPackagesToScan())
                    .packagesToExclude(this.swaggerProperties.getGroup().getCurrentApi().getPackagesToExclude())
                    .pathsToMatch(this.swaggerProperties.getGroup().getCurrentApi().getPathsToMatch().split(",\\s*"))
                    .addOpenApiCustomizer(securityCustomizer)
                    .addOpenApiCustomizer(sortOperations)
                    .addOpenApiCustomizer(sortTags)
                    .addOpenApiCustomizer(serverUrlCustomizer)
                    .build();
        }
        return null;
    }

    @Bean
    @Qualifier("serverUrlCustomizer")
    public OpenApiCustomizer serverUrlCustomizer() {
        return openApi -> openApi.setServers(
                List.of(new Server()
                        .url(this.swaggerProperties.getServerUrl())));
    }

}
