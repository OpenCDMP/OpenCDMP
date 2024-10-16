package org.opencdmp.model.builder.actionconfirmation;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.actionconfirmation.UserInviteToTenantRequestEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.actionconfirmation.UserInviteToTenantRequest;
import org.opencdmp.model.builder.BaseBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserInviteToTenantRequestBuilder extends BaseBuilder<UserInviteToTenantRequest, UserInviteToTenantRequestEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UserInviteToTenantRequestBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserInviteToTenantRequestBuilder.class)));
    }

    public UserInviteToTenantRequestBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UserInviteToTenantRequest> build(FieldSet fields, List<UserInviteToTenantRequestEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<UserInviteToTenantRequest> models = new ArrayList<>();
        for (UserInviteToTenantRequestEntity d : data) {
            UserInviteToTenantRequest m = new UserInviteToTenantRequest();
            if (fields.hasField(this.asIndexer(UserInviteToTenantRequest._email))) m.setEmail(d.getEmail());
            if (fields.hasField(this.asIndexer(UserInviteToTenantRequest._tenantCode))) m.setTenantCode(d.getTenantCode());
            if (fields.hasField(this.asIndexer(UserInviteToTenantRequest._roles))) m.setRoles(d.getRoles());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
