package org.opencdmp.model.builder.commonmodels;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.ContactInfoType;
import org.opencdmp.commonmodels.models.UserContactInfoModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserContactInfoEntity;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserContactInfoCommonModelBuilder extends BaseCommonModelBuilder<UserContactInfoModel, UserContactInfoEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UserContactInfoCommonModelBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserContactInfoCommonModelBuilder.class)));
    }

    public UserContactInfoCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<UserContactInfoModel, UserContactInfoEntity>> buildInternal(List<UserContactInfoEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<UserContactInfoModel, UserContactInfoEntity>> models = new ArrayList<>();
        for (UserContactInfoEntity d : data) {
            UserContactInfoModel m = new UserContactInfoModel();
            m.setId(d.getId());
            m.setCreatedAt(d.getCreatedAt());
            m.setValue(d.getValue());
            switch (d.getType()){
                case Email -> m.setType(ContactInfoType.Email);
                default -> throw new MyApplicationException("unrecognized type " + d.getType());
            }
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
