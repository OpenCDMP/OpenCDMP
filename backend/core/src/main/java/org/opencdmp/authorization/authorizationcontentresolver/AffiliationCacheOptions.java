package org.opencdmp.authorization.authorizationcontentresolver;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.affiliation")
public class AffiliationCacheOptions extends CacheOptions {
}
