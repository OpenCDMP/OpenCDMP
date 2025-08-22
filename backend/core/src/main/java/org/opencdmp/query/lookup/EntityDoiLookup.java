package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.EntityDoiQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class EntityDoiLookup extends Lookup {

    @Schema(description = SwaggerHelpers.EntityDoi.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.EntityDoi.types_description)
    private List<EntityType> types;

    @Schema(description = SwaggerHelpers.EntityDoi.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.EntityDoi.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.EntityDoi.dois_description)
    private List<String> dois;

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<EntityType> getTypes() {
        return types;
    }

    public void setTypes(List<EntityType> types) {
        this.types = types;
    }

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<String> getDois() {
        return dois;
    }

    public void setDois(List<String> dois) {
        this.dois = dois;
    }

    public EntityDoiQuery enrich(QueryFactory queryFactory) {
        EntityDoiQuery query = queryFactory.query(EntityDoiQuery.class);
        if (this.isActive != null)
            query.isActive(this.isActive);
        if (this.types != null)
            query.types(this.types);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);
        if (this.dois != null) {
            query.dois(this.dois);
        }

        this.enrichCommon(query);

        return query;
    }

}
