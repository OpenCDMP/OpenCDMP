package org.opencdmp.service.dashborad;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DashboardServiceProperties.class)
public class DashboardServiceConfiguration {

}
