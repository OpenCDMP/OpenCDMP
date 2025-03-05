package org.opencdmp.model.builder.evaluator;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.evaluator.EvaluatorSource;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluatorSourceBuilder extends BaseBuilder<EvaluatorSource, EvaluatorSourceEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private boolean encrypted;

    @Autowired
    public EvaluatorSourceBuilder(
            ConventionService conventionService, EncryptionService encryptionService, TenantProperties tenantProperties) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EvaluatorSourceBuilder.class)));
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
    }

    public  EvaluatorSourceBuilder authorize(EnumSet<AuthorizationFlags> values){
        this.authorize = values;
        return this;
    }

    public EvaluatorSourceBuilder encrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    @Override
    public List<EvaluatorSource> build(FieldSet fields, List<EvaluatorSourceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

        List<EvaluatorSource> models = new ArrayList<>();
        for(EvaluatorSourceEntity d : data){
            EvaluatorSource m = new EvaluatorSource();
            if (fields.hasField(this.asIndexer(EvaluatorSource._evaluatorId))) m.setEvaluatorId(d.getEvaluatorId());
            if (fields.hasField(this.asIndexer(EvaluatorSource._url))) m.setUrl(d.getUrl());
            if (fields.hasField(this.asIndexer(EvaluatorSource._issuerUrl))) m.setIssuerUrl(d.getIssuerUrl());
            if (fields.hasField(this.asIndexer(EvaluatorSource._clientId))) m.setClientId(d.getClientId());
            if (fields.hasField(this.asIndexer(EvaluatorSource._clientSecret))){
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
            if (fields.hasField(this.asIndexer(EvaluatorSource._scope))) m.setScope(d.getScope());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
