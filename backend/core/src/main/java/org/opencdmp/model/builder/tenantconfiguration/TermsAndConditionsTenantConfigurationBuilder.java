package org.opencdmp.model.builder.tenantconfiguration;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.TermsAndConditionsTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.TermsAndConditionsTenantConfiguration;
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
public class TermsAndConditionsTenantConfigurationBuilder extends BaseBuilder<TermsAndConditionsTenantConfiguration, TermsAndConditionsTenantConfigurationEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public TermsAndConditionsTenantConfigurationBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(TermsAndConditionsTenantConfigurationBuilder.class)));
    }

    public TermsAndConditionsTenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<TermsAndConditionsTenantConfiguration> build(FieldSet fields, List<TermsAndConditionsTenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

        List<TermsAndConditionsTenantConfiguration> models = new ArrayList<>();
        for (TermsAndConditionsTenantConfigurationEntity d : data) {
            TermsAndConditionsTenantConfiguration m = new TermsAndConditionsTenantConfiguration();
            if (fields.hasField(this.asIndexer(TermsAndConditionsTenantConfiguration._content))) m.setContent(d.getContent());
            if (fields.hasField(this.asIndexer(TermsAndConditionsTenantConfiguration._version))) m.setVersion(d.getVersion());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
