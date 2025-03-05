package org.opencdmp.service.evaluator;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.evaluator-sources-by-tenant")
public class EvaluatorSourcesCacheOptions extends CacheOptions {
}
