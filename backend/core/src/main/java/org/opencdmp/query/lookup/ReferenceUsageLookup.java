package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.ReferenceUsageEntityType;

import java.util.Collection;
import java.util.UUID;

/**
 * Asks which references the plans (or descriptions) visible to the caller link to, with counts.
 * Paging applies to the references, ordered by count (descending) and then label.
 */
public class ReferenceUsageLookup extends Lookup {

    @Schema(description = "Count usage in plans (0) or descriptions (1). Defaults to plans.")
    private ReferenceUsageEntityType entityType;

    @Schema(description = "Only references of these reference types.")
    private Collection<UUID> typeIds;

    @Schema(description = "Leave out references of these reference types.")
    private Collection<UUID> excludedTypeIds;

    @Schema(description = "Free-text match on the reference label, description or reference code.")
    private String like;

    public ReferenceUsageEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ReferenceUsageEntityType entityType) {
        this.entityType = entityType;
    }

    public Collection<UUID> getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(Collection<UUID> typeIds) {
        this.typeIds = typeIds;
    }

    public Collection<UUID> getExcludedTypeIds() {
        return excludedTypeIds;
    }

    public void setExcludedTypeIds(Collection<UUID> excludedTypeIds) {
        this.excludedTypeIds = excludedTypeIds;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }
}
