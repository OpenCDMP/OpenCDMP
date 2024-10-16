package org.opencdmp.model.builder.commonmodels;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.deposit.DepositConfiguration;
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
public class DepositConfigurationBuilder extends BaseBuilder<DepositConfiguration, org.opencdmp.depositbase.repository.DepositConfiguration> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DepositConfigurationBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DepositConfigurationBuilder.class)));
    }

    public DepositConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DepositConfiguration> build(FieldSet fields, List<org.opencdmp.depositbase.repository.DepositConfiguration> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DepositConfiguration> models = new ArrayList<>();
        for (org.opencdmp.depositbase.repository.DepositConfiguration d : data) {
            DepositConfiguration m = new DepositConfiguration();
            if (fields.hasField(this.asIndexer(DepositConfiguration._depositType))) m.setDepositType(d.getDepositType());
            if (fields.hasField(this.asIndexer(DepositConfiguration._redirectUri))) m.setRedirectUri(d.getRedirectUri());
            if (fields.hasField(this.asIndexer(DepositConfiguration._repositoryId))) m.setRepositoryId(d.getRepositoryId());
            if (fields.hasField(this.asIndexer(DepositConfiguration._repositoryAuthorizationUrl))) m.setRepositoryAuthorizationUrl(d.getRepositoryAuthorizationUrl());
            if (fields.hasField(this.asIndexer(DepositConfiguration._repositoryRecordUrl))) m.setRepositoryRecordUrl(d.getRepositoryRecordUrl());
            if (fields.hasField(this.asIndexer(DepositConfiguration._repositoryClientId))) m.setRepositoryClientId(d.getRepositoryClientId());
            if (fields.hasField(this.asIndexer(DepositConfiguration._hasLogo))) m.setHasLogo(d.isHasLogo());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
