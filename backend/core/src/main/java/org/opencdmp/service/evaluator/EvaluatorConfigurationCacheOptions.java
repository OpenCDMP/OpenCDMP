package org.opencdmp.service.evaluator;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.evaluator-config-by-id")
public class EvaluatorConfigurationCacheOptions extends CacheOptions {
}
