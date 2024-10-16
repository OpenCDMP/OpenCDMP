package org.opencdmp.service.supportivematerial;


import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.supportive-material")
public class SupportiveMaterialCacheOptions extends CacheOptions {
}
