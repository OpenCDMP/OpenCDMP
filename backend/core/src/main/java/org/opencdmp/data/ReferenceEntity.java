package org.opencdmp.data;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.ReferenceSourceTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"Reference\"")
public class ReferenceEntity extends TenantScopedBaseEntity {

    public static class KnownFields {
        public static final String Key = "key";
        public static final String ReferenceId = "reference_id";
        public static final String Abbreviation = "abbreviation";
        public static final String Description = "description";
        public static final String Label = "label";
        public static final String SourceLabel = "tag";
    }

    
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    public static final String _id = "id";

    @Column(name = "label", length = _labelLength, nullable = false)
    private String label;

    public static final String _label = "label";

    public static final int _labelLength = 1024;

    @Column(name = "type", nullable = false)
    private UUID typeId;

    public static final String _typeId = "typeId";

    @Column(name = "description")
    private String description;

    public static final String _description = "description";

    @Column(name = "definition")
    private String definition;

    public static final String _definition = "definition";

    @Column(name = "reference", length = _referenceLength, nullable = false)
    private String reference;

    public static final String _reference = "reference";

    public static final int _referenceLength = 1024;

    @Column(name = "abbreviation", length = _abbreviationLength)
    private String abbreviation;

    public static final String _abbreviation = "abbreviation";

    public static final int _abbreviationLength = 50;

    @Column(name = "source", length = _sourceLength)
    private String source;

    public static final String _source = "source";

    public static final int _sourceLength = 1024;

    @Column(name = "source_type", nullable = false)
    @Convert(converter = ReferenceSourceTypeConverter.class)
    private ReferenceSourceType sourceType;

    public static final String _sourceType = "sourceType";

    @Column(name = "is_active", nullable = false)
    @Convert(converter = IsActiveConverter.class)
    private IsActive isActive;

    public static final String _isActive = "isActive";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdById;

    public static final String _createdById = "createdById";

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

    public UUID getTypeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ReferenceSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ReferenceSourceType referenceSourceType) {
        this.sourceType = referenceSourceType;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

}
