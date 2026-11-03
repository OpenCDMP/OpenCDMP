package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.DashboardSlideTranslationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.tenantconfiguration.DashboardSlideTranslation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DashboardSlideTranslationBuilder extends BaseBuilder<DashboardSlideTranslation, DashboardSlideTranslationEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DashboardSlideTranslationBuilder(ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DashboardSlideTranslationBuilder.class)));
    }

    public DashboardSlideTranslationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DashboardSlideTranslation> build(FieldSet fields, List<DashboardSlideTranslationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

        List<DashboardSlideTranslation> models = new ArrayList<>();
        for (DashboardSlideTranslationEntity d : data) {
            DashboardSlideTranslation m = new DashboardSlideTranslation();
            if (fields.hasField(this.asIndexer(DashboardSlideTranslation._languageCode))) m.setLanguageCode(d.getLanguageCode());
            if (fields.hasField(this.asIndexer(DashboardSlideTranslation._title))) m.setTitle(d.getTitle());
            if (fields.hasField(this.asIndexer(DashboardSlideTranslation._text))) m.setText(d.getText());
            if (fields.hasField(this.asIndexer(DashboardSlideTranslation._buttonText))) m.setButtonText(d.getButtonText());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
