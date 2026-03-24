package org.opencdmp.configurations;

import io.swagger.v3.oas.models.servers.Server;
import org.opencdmp.configurations.swagger.SwaggerConfigProperties;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiGroupConfig {

    private final SwaggerConfigProperties swaggerConfigProperties;

    public OpenApiGroupConfig(SwaggerConfigProperties swaggerConfigProperties) {
        this.swaggerConfigProperties = swaggerConfigProperties;
    }

    @Bean
    public GroupedOpenApi legacyApi( @Qualifier("securityCustomizer") OpenApiCustomizer securityCustomizer,
                                     @Qualifier("sortOperations") OpenApiCustomizer sortOperations,
                                     @Qualifier("sortTags") OpenApiCustomizer sortTags,
                                     @Qualifier("serverUrlCustomizer") OpenApiCustomizer serverUrlCustomizer
    ) {
        if (this.swaggerConfigProperties.getGroup().getLegacyApi() != null) {
            return GroupedOpenApi.builder()
                    .group(this.swaggerConfigProperties.getGroup().getLegacyApi().getGroup())
                    .displayName(this.swaggerConfigProperties.getGroup().getLegacyApi().getDisplayName())
                    .packagesToScan(this.swaggerConfigProperties.getGroup().getLegacyApi().getPackagesToScan())
                    .pathsToMatch(this.swaggerConfigProperties.getGroup().getLegacyApi().getPathsToMatch().split(",\\s*"))
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
        if (this.swaggerConfigProperties.getGroup().getCurrentApi() != null) {
            return GroupedOpenApi.builder()
                    .group(this.swaggerConfigProperties.getGroup().getCurrentApi().getGroup())
                    .displayName(this.swaggerConfigProperties.getGroup().getCurrentApi().getDisplayName())
                    .packagesToScan(this.swaggerConfigProperties.getGroup().getCurrentApi().getPackagesToScan())
                    .packagesToExclude(this.swaggerConfigProperties.getGroup().getCurrentApi().getPackagesToExclude())
                    .pathsToMatch(this.swaggerConfigProperties.getGroup().getCurrentApi().getPathsToMatch().split(",\\s*"))
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
                        .url(this.swaggerConfigProperties.getServerUrl())));
    }

}
