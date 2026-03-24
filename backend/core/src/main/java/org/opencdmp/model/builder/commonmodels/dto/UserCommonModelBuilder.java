package org.opencdmp.model.builder.commonmodels.dto;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.user.UserModel;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.user.User;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dto.UserCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserCommonModelBuilder extends BaseCommonModelBuilder<User, UserModel> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None); 
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;

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

    @Override
    protected List<CommonModelBuilderItemResponse<User, UserModel>> buildInternal(List<UserModel> data) throws MyApplicationException {
        this.logger.debug("building for {} items ", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null)
            return new ArrayList<>();

        List<CommonModelBuilderItemResponse<User, UserModel>> models = new ArrayList<>();
        for (UserModel d : data) {
            User m = new User();
            m.setId(d.getId());
            m.setName(d.getName());
            m.setContacts(this.builderFactory.builder(UserContactInfoCommonModelBuilder.class).authorize(this.authorize).build(d.getContacts()));
            m.setIsActive(IsActive.Active);
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
