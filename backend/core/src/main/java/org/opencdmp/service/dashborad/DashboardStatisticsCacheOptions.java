package org.opencdmp.service.dashborad;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.dashboard-statistics-by-user-id")
public class DashboardStatisticsCacheOptions extends CacheOptions {
}
