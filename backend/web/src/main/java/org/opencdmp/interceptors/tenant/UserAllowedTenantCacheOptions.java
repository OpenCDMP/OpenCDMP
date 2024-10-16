package org.opencdmp.interceptors.tenant;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.user-allowed-tenant")
public class UserAllowedTenantCacheOptions extends CacheOptions {
}


