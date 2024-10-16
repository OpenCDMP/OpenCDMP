package org.opencdmp.model.builder.planreference;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planreference.PlanReferenceDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planreference.PlanReferenceData;
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

@Component("referenceplanreferencedatabuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanReferenceDataBuilder extends BaseBuilder<PlanReferenceData, PlanReferenceDataEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanReferenceDataBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanReferenceDataBuilder.class)));
    }

    public PlanReferenceDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanReferenceData> build(FieldSet fields, List<PlanReferenceDataEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        
        List<PlanReferenceData> models = new ArrayList<>();
        for (PlanReferenceDataEntity d : data) {
            PlanReferenceData m = new PlanReferenceData();
            if (fields.hasField(this.asIndexer(PlanReferenceData._blueprintFieldId))) m.setBlueprintFieldId(d.getBlueprintFieldId());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
