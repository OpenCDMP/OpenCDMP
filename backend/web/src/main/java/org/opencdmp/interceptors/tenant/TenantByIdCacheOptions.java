package org.opencdmp.interceptors.tenant;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.tenant-by-id")
public class TenantByIdCacheOptions extends CacheOptions {
}
