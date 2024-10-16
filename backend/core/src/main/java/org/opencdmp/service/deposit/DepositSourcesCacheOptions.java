package org.opencdmp.service.deposit;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.deposit-sources-by-tenant")
public class DepositSourcesCacheOptions extends CacheOptions {
}
