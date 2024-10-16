package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class BaseFieldDataBuilder<Model extends BaseFieldData, Entity extends BaseFieldDataEntity> extends BaseBuilder<Model, Entity> {

    protected EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public BaseFieldDataBuilder(
            ConventionService conventionService,
            LoggerService logger) {
        super(conventionService, logger);
    }

    public BaseFieldDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }
    
    protected abstract Model getInstance();

    protected abstract void buildChild(FieldSet fields, Entity d, Model m);
    @Override
    public List<Model> build(FieldSet fields, List<Entity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<Model> models = new ArrayList<>();
        for (Entity d : data) {
            Model m = this.getInstance();
            if (fields.hasField(this.asIndexer(Model._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Model._fieldType))) m.setFieldType(d.getFieldType());
            this.buildChild(fields, d, m);
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}

