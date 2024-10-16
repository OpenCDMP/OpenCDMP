package org.opencdmp.model.builder.usercredential;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.usercredential.UserCredentialDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.usercredential.UserCredentialData;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component()
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserCredentialDataBuilder extends BaseBuilder<UserCredentialData, UserCredentialDataEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UserCredentialDataBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserCredentialDataBuilder.class)));
    }

    public UserCredentialDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UserCredentialData> build(FieldSet fields, List<UserCredentialDataEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        
        List<UserCredentialData> models = new ArrayList<>();
        for (UserCredentialDataEntity d : data) {
            UserCredentialData m = new UserCredentialData();
            if (fields.hasField(this.asIndexer(UserCredentialData._email))) m.setEmail(d.getEmail());
            if (fields.hasField(this.asIndexer(UserCredentialData._externalProviderNames))) m.setExternalProviderNames(d.getExternalProviderNames());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
