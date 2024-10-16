package org.opencdmp.interceptors.tenant;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TenantScopeProperties.class)
public class TenantScopeConfiguration {
}
