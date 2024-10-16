package org.opencdmp.service.elastic;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.opencdmp.elastic.data.PlanElasticEntity;
import org.opencdmp.model.PublicDescription;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.builder.PublicDescriptionBuilder;
import org.opencdmp.model.builder.PublicPlanBuilder;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.lookup.DescriptionLookup;
import org.opencdmp.query.lookup.PlanLookup;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

@Service
public class ElasticQueryHelperServiceImpl implements ElasticQueryHelperService {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ElasticQueryHelperServiceImpl.class));

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final ElasticService elasticService;
	private final AppElasticConfiguration appElasticConfiguration;
	public ElasticQueryHelperServiceImpl(QueryFactory queryFactory, BuilderFactory builderFactory, ElasticService elasticService, AppElasticConfiguration appElasticConfiguration) {
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.elasticService = elasticService;
		this.appElasticConfiguration = appElasticConfiguration;
	}

	@Override
	public QueryResult<Plan> collect(PlanLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet) {
		EnumSet<AuthorizationFlags> flags = authorizationFlags == null ? EnumSet.of(AuthorizationFlags.None) : authorizationFlags;
		return this.collect(lookup, (d) -> this.builderFactory.builder(PlanBuilder.class).authorize(flags).build(fieldSet != null ? fieldSet : lookup.getProject(), d), flags);
	}

	@Override
	public QueryResult<PublicPlan> collectPublic(PlanLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet) {
		EnumSet<AuthorizationFlags> flags = authorizationFlags == null ? EnumSet.of(AuthorizationFlags.None) : authorizationFlags;
		return this.collect(lookup, (d) -> this.builderFactory.builder(PublicPlanBuilder.class).authorize(flags).build(fieldSet != null ? fieldSet : lookup.getProject(), d), flags);
	}

	private <M> QueryResult<M> collect(PlanLookup lookup, Function<List<PlanEntity>, List<M>> buildFunc, EnumSet<AuthorizationFlags> flags) {
		PlanQuery query = null;
		QueryResult<M> result = new QueryResult<>();
		boolean elasticFilterUsed = false;
		if (lookup.useElastic() && this.elasticService.enabled()){
			try {
				List<PlanElasticEntity> elasticResponse = lookup.enrichElastic(this.queryFactory).authorize(flags).collectAs(new BaseFieldSet().ensure(PlanElasticEntity._id));
				query = this.queryFactory.query(PlanQuery.class).authorize(flags).ids(elasticResponse.stream().map(PlanElasticEntity::getId).toList());
				query.setOrder(lookup.enrich(this.queryFactory).getOrder());
				if (lookup.getMetadata() != null && lookup.getMetadata().countAll) result.setCount(lookup.enrichElastic(this.queryFactory).authorize(flags).count());
				elasticFilterUsed = true;
			} catch (Exception e){
				elasticFilterUsed = false;
				if (this.appElasticConfiguration.getElasticQueryHelperServiceProperties().getEnableDbFallback()) {
					logger.error(e.getMessage(), e);
				} else {
					throw e;
				}
			}
		} 
		
		if (!elasticFilterUsed) {
			query = lookup.enrich(this.queryFactory).disableTracking().authorize(flags);
			if (lookup.getMetadata() != null && lookup.getMetadata().countAll) result.setCount(query.count());
		}
		result.setItems(buildFunc.apply(query.collect()));
		if (lookup.getMetadata() == null || !lookup.getMetadata().countAll) result.setCount(result.getItems() != null ? result.getItems().size() : 0);

		return result;
	}

	@Override
	public long count(PlanLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags) {
		EnumSet<AuthorizationFlags> flags = authorizationFlags == null ? EnumSet.of(AuthorizationFlags.None) : authorizationFlags;
		if (lookup.useElastic() && this.elasticService.enabled()){
			try {
				return lookup.enrichElastic(this.queryFactory).authorize(flags).count();
			} catch (Exception e){
				if (this.appElasticConfiguration.getElasticQueryHelperServiceProperties().getEnableDbFallback()) {
					logger.error(e.getMessage(), e);
				} else {
					throw e;
				}
			}
		}
		return lookup.enrich(this.queryFactory).authorize(flags).count();
	}

	@Override
	public  QueryResult<Description> collect(DescriptionLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet) {
		EnumSet<AuthorizationFlags> flags = authorizationFlags == null ? EnumSet.of(AuthorizationFlags.None) : authorizationFlags;
		return this.collect(lookup, (d) -> this.builderFactory.builder(DescriptionBuilder.class).authorize(flags).build(fieldSet != null ? fieldSet : lookup.getProject(), d), flags);
	}
	

	@Override
	public QueryResult<PublicDescription> collectPublic(DescriptionLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet) {
		EnumSet<AuthorizationFlags> flags = authorizationFlags == null ? EnumSet.of(AuthorizationFlags.None) : authorizationFlags;
		return this.collect(lookup, (d) -> this.builderFactory.builder(PublicDescriptionBuilder.class).authorize(flags).build(fieldSet != null ? fieldSet : lookup.getProject(), d), flags);
	}

	private <M> QueryResult<M> collect(DescriptionLookup lookup, Function<List<DescriptionEntity>, List<M>> buildFunc, EnumSet<AuthorizationFlags> flags) {
		DescriptionQuery query = null;
		QueryResult<M> result = new QueryResult<>();
		boolean elasticFilterUsed = false;
		if (lookup.useElastic() && this.elasticService.enabled()){
			
			try {
				List<DescriptionElasticEntity> elasticResponse = lookup.enrichElastic(this.queryFactory).authorize(flags).collectAs(new BaseFieldSet().ensure(DescriptionElasticEntity._id));
				query = this.queryFactory.query(DescriptionQuery.class).authorize(flags).ids(elasticResponse.stream().map(DescriptionElasticEntity::getId).toList());
				query.setOrder(lookup.enrich(this.queryFactory).getOrder());
				if (lookup.getMetadata() != null && lookup.getMetadata().countAll) result.setCount(lookup.enrichElastic(this.queryFactory).authorize(flags).count());
				elasticFilterUsed = true;
			} catch (Exception e){
				elasticFilterUsed = false;
				if (this.appElasticConfiguration.getElasticQueryHelperServiceProperties().getEnableDbFallback()) {
					logger.error(e.getMessage(), e);
				} else {
					throw e;
				}
			}
			
		} 
		if (!elasticFilterUsed)  {
			query = lookup.enrich(this.queryFactory).disableTracking().authorize(flags);
			if (lookup.getMetadata() != null && lookup.getMetadata().countAll) result.setCount(query.count());
		}
		result.setItems(buildFunc.apply(query.collect()));
		if (lookup.getMetadata() == null || !lookup.getMetadata().countAll) result.setCount(result.getItems() != null ? result.getItems().size() : 0);

		return result;
	}

	@Override
	public long count(DescriptionLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags) {
		EnumSet<AuthorizationFlags> flags = authorizationFlags == null ? EnumSet.of(AuthorizationFlags.None) : authorizationFlags;
		if (lookup.useElastic() && this.elasticService.enabled()){
			try {
				return lookup.enrichElastic(this.queryFactory).authorize(flags).count();
			} catch (Exception e){
				if (this.appElasticConfiguration.getElasticQueryHelperServiceProperties().getEnableDbFallback()) {
					logger.error(e.getMessage(), e);
				} else {
					throw e;
				}
			}
		} 
		return lookup.enrich(this.queryFactory).authorize(flags).count();
	}
}
