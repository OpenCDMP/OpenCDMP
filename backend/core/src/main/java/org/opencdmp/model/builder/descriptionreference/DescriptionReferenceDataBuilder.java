package org.opencdmp.model.builder.descriptionreference;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionreference.DescriptionReferenceDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptionreference.DescriptionReferenceData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("referencedescriptionreferencedatabuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionReferenceDataBuilder extends BaseBuilder<DescriptionReferenceData, DescriptionReferenceDataEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionReferenceDataBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionReferenceDataBuilder.class)));
    }

    public DescriptionReferenceDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionReferenceData> build(FieldSet fields, List<DescriptionReferenceDataEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        
        List<DescriptionReferenceData> models = new ArrayList<>();
        for (DescriptionReferenceDataEntity d : data) {
            DescriptionReferenceData m = new DescriptionReferenceData();
            if (fields.hasField(this.asIndexer(DescriptionReferenceData._fieldId))) m.setFieldId(d.getFieldId());
            if (fields.hasField(this.asIndexer(DescriptionReferenceData._ordinal))) m.setOrdinal(d.getOrdinal());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
