package org.opencdmp.service.elastic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppElasticProperties.class, ElasticQueryHelperServiceProperties.class })
public class AppElasticConfiguration {
	private final AppElasticProperties appElasticProperties;
	private final ElasticQueryHelperServiceProperties elasticQueryHelperServiceProperties;

	@Autowired
	public AppElasticConfiguration(AppElasticProperties appElasticProperties, ElasticQueryHelperServiceProperties elasticQueryHelperServiceProperties) {
		this.appElasticProperties = appElasticProperties;
		this.elasticQueryHelperServiceProperties = elasticQueryHelperServiceProperties;
	}

	public AppElasticProperties getAppElasticProperties() {
		return this.appElasticProperties;
	}

	public ElasticQueryHelperServiceProperties getElasticQueryHelperServiceProperties() {
		return this.elasticQueryHelperServiceProperties;
	}
}
