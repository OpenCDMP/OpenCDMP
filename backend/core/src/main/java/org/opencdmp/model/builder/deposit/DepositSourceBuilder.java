package org.opencdmp.model.builder.deposit;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.deposit.DepositSource;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.tenant.TenantProperties;
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
public class DepositSourceBuilder extends BaseBuilder<DepositSource, DepositSourceEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private boolean encrypted;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;

    @Autowired
    public DepositSourceBuilder(
		    ConventionService conventionService, EncryptionService encryptionService, TenantProperties tenantProperties) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DepositSourceBuilder.class)));
	    this.encryptionService = encryptionService;
	    this.tenantProperties = tenantProperties;
    }

    public DepositSourceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DepositSourceBuilder encrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    @Override
    public List<DepositSource> build(FieldSet fields, List<DepositSourceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DepositSource> models = new ArrayList<>();
        for (DepositSourceEntity d : data) {
            DepositSource m = new DepositSource();
            if (fields.hasField(this.asIndexer(DepositSource._repositoryId))) m.setRepositoryId(d.getRepositoryId());
            if (fields.hasField(this.asIndexer(DepositSource._url))) m.setUrl(d.getUrl());
            if (fields.hasField(this.asIndexer(DepositSource._issuerUrl))) m.setIssuerUrl(d.getIssuerUrl());
            if (fields.hasField(this.asIndexer(DepositSource._clientId))) m.setClientId(d.getClientId());
            if (fields.hasField(this.asIndexer(DepositSource._clientSecret))) {
                if (encrypted) {
                    try {
                        if (!this.conventionService.isNullOrEmpty(d.getClientSecret())) m.setClientSecret(this.encryptionService.decryptAES(d.getClientSecret(), tenantProperties.getConfigEncryptionAesKey(), tenantProperties.getConfigEncryptionAesIv()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    m.setClientSecret(d.getClientSecret());
                }
            }
            if (fields.hasField(this.asIndexer(DepositSource._scope))) m.setScope(d.getScope());
            if (fields.hasField(this.asIndexer(DepositSource._pdfTransformerId))) m.setPdfTransformerId(d.getPdfTransformerId());
            if (fields.hasField(this.asIndexer(DepositSource._rdaTransformerId))) m.setRdaTransformerId(d.getRdaTransformerId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
