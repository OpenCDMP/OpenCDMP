package org.opencdmp.interceptors.user;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.user-by-subject-id")
public class UserInterceptorCacheOptions extends CacheOptions {
}
