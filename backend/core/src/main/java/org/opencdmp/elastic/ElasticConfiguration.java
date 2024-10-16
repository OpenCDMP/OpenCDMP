package org.opencdmp.elastic;


import org.opencdmp.elastic.converter.*;
import gr.cite.tools.elastic.configuration.AbstractElasticConfiguration;
import gr.cite.tools.elastic.configuration.ElasticCertificateProvider;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.util.List;

@Configuration
@EnableConfigurationProperties(ElasticProperties.class)
@ConditionalOnProperty(prefix = "elastic", name = "enabled", matchIfMissing = false)
public class ElasticConfiguration extends AbstractElasticConfiguration {

	public ElasticConfiguration(ElasticProperties elasticProperties, ElasticCertificateProvider elasticCertificateProvider) {
		super(elasticProperties, elasticCertificateProvider);
	}

	@Bean
	@Override
	public ElasticsearchCustomConversions elasticsearchCustomConversions() {
		return new ElasticsearchCustomConversions(
				List.of(
						new PlanUserRoleToShortConverter(),
						new DescriptionTemplateVersionStatusToShortConverter(),
						new PlanStatusToShortConverter(),
						new DescriptionStatusToShortConverter(),
						new IsActiveToShortConverter(),
						new PlanVersionStatusToShortConverter(),
						new PlanAccessTypeToShortConverter(),

						new ShortToPlanStatusConverter(),
						new ShortToPlanUserRoleConverter(),
						new ShortToDescriptionTemplateVersionStatusConverter(),
						new ShortToPlanStatusConverter(),
						new ShortToDescriptionStatusConverter(),
						new ShortToIsActiveConverter(),
						new ShortToPlanVersionStatusConverter(),
						new ShortToPlanAccessTypeConverter()
				));
	}
}
