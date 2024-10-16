package org.opencdmp.model.builder.externalfetcher;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.AuthenticationConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.externalfetcher.AuthenticationConfiguration;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AuthenticationConfigurationBuilder extends BaseBuilder<AuthenticationConfiguration, AuthenticationConfigurationEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public AuthenticationConfigurationBuilder(
		    ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(AuthenticationConfigurationBuilder.class)));
    }

    public AuthenticationConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<AuthenticationConfiguration> build(FieldSet fields, List<AuthenticationConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<AuthenticationConfiguration> models = new ArrayList<>();
        for (AuthenticationConfigurationEntity d : data) {
            AuthenticationConfiguration m = new AuthenticationConfiguration();
            if (fields.hasField(this.asIndexer(AuthenticationConfiguration._enabled))) m.setEnabled(d.getEnabled());
            if (fields.hasField(this.asIndexer(AuthenticationConfiguration._authUrl))) m.setAuthUrl(d.getAuthUrl());
            if (fields.hasField(this.asIndexer(AuthenticationConfiguration._authMethod))) m.setAuthMethod(d.getAuthMethod());
            if (fields.hasField(this.asIndexer(AuthenticationConfiguration._authTokenPath))) m.setAuthTokenPath(d.getAuthTokenPath());
            if (fields.hasField(this.asIndexer(AuthenticationConfiguration._authRequestBody))) m.setAuthRequestBody(d.getAuthRequestBody());
            if (fields.hasField(this.asIndexer(AuthenticationConfiguration._type))) m.setType(d.getType());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
