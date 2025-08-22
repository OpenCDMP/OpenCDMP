package org.opencdmp.model.builder.commonmodels;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.UserContactInfoModel;
import org.opencdmp.commonmodels.models.user.UserModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserContactInfoEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.query.UserContactInfoQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserCommonModelBuilder extends BaseCommonModelBuilder<UserModel, UserEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None); 
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private boolean disableContacts;

    @Autowired
    public UserCommonModelBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public UserCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public UserCommonModelBuilder disableContacts(boolean values) {
        this.disableContacts = this.disableContacts;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<UserModel, UserEntity>> buildInternal(List<UserEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items ", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null)
            return new ArrayList<>();
        Map<UUID, List<UserContactInfoModel>> userContactInfoModelMap = this.disableContacts ? null : this.collectUserContactInfos(data);

        List<CommonModelBuilderItemResponse<UserModel, UserEntity>> models = new ArrayList<>();
        for (UserEntity d : data) {
            UserModel m = new UserModel();
            m.setId(d.getId());
            m.setName(d.getName());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
            if (userContactInfoModelMap != null && d.getId() != null && userContactInfoModelMap.containsKey(d.getId())) m.setContacts(userContactInfoModelMap.get(d.getId()));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, List<UserContactInfoModel>> collectUserContactInfos(List<UserEntity> data) throws MyApplicationException {
        this.logger.debug("checking related - {}", UserContactInfo.class.getSimpleName());

        Map<UUID, List<UserContactInfoModel>> itemMap;
        UserContactInfoQuery query = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().authorize(this.authorize).userIds(data.stream().map(UserEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserContactInfoCommonModelBuilder.class).authorize(this.authorize).asMasterKey(query, UserContactInfoEntity::getUserId);

        return itemMap;
    }

}
