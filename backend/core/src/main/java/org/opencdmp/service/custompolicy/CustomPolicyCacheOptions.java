package org.opencdmp.service.custompolicy;


import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.custom-policy-by-tenant")
public class CustomPolicyCacheOptions extends CacheOptions {
}
