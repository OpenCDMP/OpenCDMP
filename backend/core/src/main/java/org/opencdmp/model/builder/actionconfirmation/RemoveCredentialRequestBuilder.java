package org.opencdmp.model.builder.actionconfirmation;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.actionconfirmation.RemoveCredentialRequestEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.actionconfirmation.RemoveCredentialRequest;
import org.opencdmp.model.builder.BaseBuilder;
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
public class RemoveCredentialRequestBuilder extends BaseBuilder<RemoveCredentialRequest, RemoveCredentialRequestEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public RemoveCredentialRequestBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RemoveCredentialRequestBuilder.class)));
    }

    public RemoveCredentialRequestBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<RemoveCredentialRequest> build(FieldSet fields, List<RemoveCredentialRequestEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        List<RemoveCredentialRequest> models = new ArrayList<>();
        for (RemoveCredentialRequestEntity d : data) {
            RemoveCredentialRequest m = new RemoveCredentialRequest();
            if (fields.hasField(this.asIndexer(RemoveCredentialRequest._credentialId))) m.setCredentialId(d.getCredentialId());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
