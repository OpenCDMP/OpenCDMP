package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.model.PublicReferenceType;
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
public class PublicReferenceTypeBuilder extends BaseBuilder<PublicReferenceType, ReferenceTypeEntity>{

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicReferenceTypeBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicReferenceTypeBuilder.class)));
    }

    public PublicReferenceTypeBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicReferenceType> build(FieldSet fields, List<ReferenceTypeEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PublicReferenceType> models = new ArrayList<>();
        for (ReferenceTypeEntity d : data) {
            PublicReferenceType m = new PublicReferenceType();
            if (fields.hasField(this.asIndexer(PublicReferenceType._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicReferenceType._name))) m.setName(d.getName());
            if (fields.hasField(this.asIndexer(PublicReferenceType._code))) m.setCode(d.getCode());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
