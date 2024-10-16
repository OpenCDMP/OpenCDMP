package org.opencdmp.model.builder.filetransformer;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.filetransformer.FileTransformerSource;
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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FileTransformerSourceBuilder extends BaseBuilder<FileTransformerSource, FileTransformerSourceEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private boolean encrypted;
    @Autowired
    public FileTransformerSourceBuilder(
		    ConventionService conventionService, EncryptionService encryptionService, TenantProperties tenantProperties) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FileTransformerSourceBuilder.class)));
	    this.encryptionService = encryptionService;
	    this.tenantProperties = tenantProperties;
    }

    public FileTransformerSourceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public FileTransformerSourceBuilder encrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    @Override
    public List<FileTransformerSource> build(FieldSet fields, List<FileTransformerSourceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<FileTransformerSource> models = new ArrayList<>();
        for (FileTransformerSourceEntity d : data) {
            FileTransformerSource m = new FileTransformerSource();
            if (fields.hasField(this.asIndexer(FileTransformerSource._transformerId))) m.setTransformerId(d.getTransformerId());
            if (fields.hasField(this.asIndexer(FileTransformerSource._url))) m.setUrl(d.getUrl());
            if (fields.hasField(this.asIndexer(FileTransformerSource._issuerUrl))) m.setIssuerUrl(d.getIssuerUrl());
            if (fields.hasField(this.asIndexer(FileTransformerSource._clientId))) m.setClientId(d.getClientId());
            if (fields.hasField(this.asIndexer(FileTransformerSource._clientSecret))){
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
            if (fields.hasField(this.asIndexer(FileTransformerSource._scope))) m.setScope(d.getScope());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
