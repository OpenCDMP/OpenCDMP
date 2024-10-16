package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.elastic.data.nested.NestedCollaboratorElasticEntity;
import org.opencdmp.elastic.elasticbuilder.BaseElasticBuilder;
import org.opencdmp.model.user.User;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedCollaboratorElasticBuilder extends BaseElasticBuilder<NestedCollaboratorElasticEntity, PlanUserEntity> {

    private final QueryFactory queryFactory;
    @Autowired
    public NestedCollaboratorElasticBuilder(
		    ConventionService conventionService, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedCollaboratorElasticBuilder.class)));
	    this.queryFactory = queryFactory;
    }

    @Override
    public List<NestedCollaboratorElasticEntity> build(List<PlanUserEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        List<NestedCollaboratorElasticEntity> models = new ArrayList<>();
        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active).ids(data.stream().map(PlanUserEntity::getUserId).distinct().collect(Collectors.toList())).collectAs(new BaseFieldSet().ensure(User._id).ensure(User._name));
        Map<UUID, String> userNameMap = users == null ? new HashMap<>() : users.stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));
        for (PlanUserEntity d : data) {
            NestedCollaboratorElasticEntity m = new NestedCollaboratorElasticEntity();
            m.setId(d.getId());
            m.setUserId(d.getUserId());
            m.setRole(d.getRole());
            m.setName(userNameMap.getOrDefault(d.getUserId(), d.getUserId().toString())); 
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
