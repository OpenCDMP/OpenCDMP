package org.opencdmp.service.reference;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.reference")
public class ReferenceCacheOptions extends CacheOptions {
}
