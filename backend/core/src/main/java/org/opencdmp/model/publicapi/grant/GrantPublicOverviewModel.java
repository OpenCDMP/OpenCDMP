package org.opencdmp.model.publicapi.grant;

import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.publicapi.funder.FunderPublicOverviewModel;
import org.opencdmp.model.reference.Field;
import org.opencdmp.service.publicapi.PublicApiProperties;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GrantPublicOverviewModel {
    private UUID id;
    private String label;
    private String abbreviation;
    private String description;
    private Date startDate;
    private Date endDate;
    private String uri;
    private FunderPublicOverviewModel funder;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public FunderPublicOverviewModel getFunder() {
        return funder;
    }
    public void setFunder(FunderPublicOverviewModel funder) {
        this.funder = funder;
    }

    public static GrantPublicOverviewModel fromPlanReferences(List<PlanReference> references, PublicApiProperties.ReferenceTypeMapConfig config) {
        FunderPublicOverviewModel funder = null;
        for (PlanReference planReference : references) {
            if (!Objects.equals(planReference.getReference().getType().getId(), config.getFunderTypeId())) {
                funder = new FunderPublicOverviewModel();
                Reference reference = planReference.getReference();
                funder.setId(reference.getId());
                funder.setLabel(reference.getLabel());
                continue;
            }
            if (!Objects.equals(planReference.getReference().getType().getId(), config.getGrantTypeId()))
                continue;
            GrantPublicOverviewModel model = new GrantPublicOverviewModel();
            Reference reference = planReference.getReference();
            model.setId(reference.getId());
            model.setDescription(reference.getDescription());
            model.setAbbreviation(reference.getAbbreviation());
            model.setLabel(reference.getLabel());
            model.setFunder(funder);
            if (reference.getDefinition() == null) return model;
            Field startDate = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("startDate")).toList().get(0);
            if (startDate != null) model.setStartDate(Date.from(Instant.parse(startDate.getValue())));
            Field endDate = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("endDate")).toList().get(0);
            if (startDate != null) model.setEndDate(Date.from(Instant.parse(endDate.getValue())));
            Field uri = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("uri")).toList().get(0);
            if (uri != null) model.setUri(uri.getValue());

            return model;
        }
        return null;
    }

    public static GrantPublicOverviewModel fromDescriptionReference(List<DescriptionReference> references, PublicApiProperties.ReferenceTypeMapConfig config) {
        FunderPublicOverviewModel funder = null;
        for (DescriptionReference descriptionReference : references) {
            if (Objects.equals(descriptionReference.getReference().getType().getId(), config.getFunderTypeId())) {
                funder = new FunderPublicOverviewModel();
                Reference reference = descriptionReference.getReference();
                funder.setId(reference.getId());
                funder.setLabel(reference.getLabel());
                continue;
            }
            if (Objects.equals(descriptionReference.getReference().getType().getId(), config.getGrantTypeId()))
                continue;
            GrantPublicOverviewModel model = new GrantPublicOverviewModel();
            Reference reference = descriptionReference.getReference();
            model.setId(reference.getId());
            model.setDescription(reference.getDescription());
            model.setAbbreviation(reference.getAbbreviation());
            model.setLabel(reference.getLabel());
            model.setFunder(funder);
            if (reference.getDefinition() == null) return model;
            Field startDate = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("startDate")).toList().get(0);
            if (startDate != null) model.setStartDate(Date.from(Instant.parse(startDate.getValue())));
            Field endDate = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("endDate")).toList().get(0);
            if (startDate != null) model.setEndDate(Date.from(Instant.parse(endDate.getValue())));
            Field uri = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("uri")).toList().get(0);
            if (uri != null) model.setUri(uri.getValue());

            return model;
        }
        return null;
    }

}
