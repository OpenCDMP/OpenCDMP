package org.opencdmp.model.builder.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelAndMultiplicityDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SelectDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.TagBuilder;
import org.opencdmp.model.builder.reference.ReferenceBuilder;
import org.opencdmp.model.description.Field;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.query.ReferenceQuery;
import org.opencdmp.query.TagQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("description.FieldBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldBuilder extends BaseBuilder<Field, FieldEntity> {
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity;

    @Autowired
    public FieldBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public FieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public FieldBuilder withFieldEntity(org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity) {
        this.fieldEntity = fieldEntity;
        return this;
    }

    @Override
    public List<Field> build(FieldSet fields, List<FieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        FieldSet externalIdentifierFields = fields.extractPrefixed(this.asPrefix(Field._externalIdentifier));
        
        FieldType fieldType = this.fieldEntity != null && this.fieldEntity.getData() != null ? this.fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        
        FieldSet referenceFields = fields.extractPrefixed(this.asPrefix(Field._references));
        Map<UUID, Reference> referenceItemsMap = FieldType.isReferenceType(fieldType) ? this.collectReferences(referenceFields, data) : null;

        FieldSet tagFields = fields.extractPrefixed(this.asPrefix(Field._tags));
        Map<UUID, Tag> tagsItemsMap = FieldType.isTagType(fieldType) ? this.collectTags(tagFields, data) : null;

        List<Field> models = new ArrayList<>();
        for (FieldEntity d : data) {
            Field m = new Field();
            if (fields.hasField(this.asIndexer(Field._dateValue)) && FieldType.isDateType(fieldType)) m.setDateValue(d.getDateValue());
            if (fields.hasField(this.asIndexer(Field._booleanValue)) && FieldType.isBooleanType(fieldType)) m.setBooleanValue(d.getBooleanValue());
            if (fields.hasField(this.asIndexer(Field._textValue)) && FieldType.isTextType(fieldType)) m.setTextValue(d.getTextValue());
            if (fields.hasField(this.asIndexer(Field._textListValue)) && FieldType.isTextListType(fieldType)) {
                boolean isMultiSelect = true;
                if(this.fieldEntity != null && this.fieldEntity.getData() != null && (this.fieldEntity.getData().getFieldType().equals(FieldType.SELECT) 
                        || this.fieldEntity.getData().getFieldType().equals(FieldType.INTERNAL_ENTRIES_PLANS)
                        || this.fieldEntity.getData().getFieldType().equals(FieldType.INTERNAL_ENTRIES_DESCRIPTIONS))){
                    if (this.fieldEntity.getData() instanceof SelectDataEntity) isMultiSelect = ((SelectDataEntity) this.fieldEntity.getData()).getMultipleSelect();
                    else if (this.fieldEntity.getData() instanceof LabelAndMultiplicityDataEntity) isMultiSelect = ((LabelAndMultiplicityDataEntity) this.fieldEntity.getData()).getMultipleSelect();

                    if (!isMultiSelect && !this.conventionService.isListNullOrEmpty(d.getTextListValue())){
                        m.setTextValue(d.getTextListValue().stream().findFirst().orElse(null));
                    }else{
                        m.setTextListValue(d.getTextListValue());
                    }
                } else{
                    m.setTextListValue(d.getTextListValue());
                }
            }
            if (!referenceFields.isEmpty() &&  FieldType.isReferenceType(fieldType)  && referenceItemsMap != null && d.getTextListValue() != null && !d.getTextListValue().isEmpty()) {
                m.setReferences(new ArrayList<>());
                for (UUID referenceId : d.getTextListValue().stream().map(UUID::fromString).distinct().toList()){
                    if (referenceItemsMap.containsKey(referenceId)) m.getReferences().add(referenceItemsMap.get(referenceId));
                }
            }

            if (!tagFields.isEmpty() &&  FieldType.isTagType(fieldType)  && tagsItemsMap != null && d.getTextListValue() != null && !d.getTextListValue().isEmpty()) {
                m.setTags(new ArrayList<>());
                for (UUID tagId : d.getTextListValue().stream().map(UUID::fromString).distinct().toList()){
                    if (tagsItemsMap.containsKey(tagId)) m.getTags().add(tagsItemsMap.get(tagId));
                }
            }
            if (!externalIdentifierFields.isEmpty() && d.getExternalIdentifier() != null && FieldType.isExternalIdentifierType(fieldType))  m.setExternalIdentifier(this.builderFactory.builder(ExternalIdentifierBuilder.class).authorize(this.authorize).build(externalIdentifierFields, d.getExternalIdentifier()));
            
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, Reference> collectReferences(FieldSet fields, List<FieldEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Reference.class.getSimpleName());

        Map<UUID, Reference> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Reference._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(FieldEntity::getTextListValue).flatMap(List::stream).map(UUID::fromString).distinct().collect(Collectors.toList()),
                    x -> {
                        Reference item = new Reference();
                        item.setId(x);
                        return item;
                    },
                    Reference::getId);
        } else {
            List<UUID> ids = new ArrayList<>();
            for (FieldEntity field: data) {
                if (field.getTextListValue() != null) {
                    for (String value: field.getTextListValue()) {
                        if (value != null && !ids.contains(UUID.fromString(value))) ids.add(UUID.fromString(value));
                    }
                }
            }
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Reference._id);
            ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(this.authorize).ids(ids);
            itemMap = this.builderFactory.builder(ReferenceBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Reference::getId);
        }

        if (!fields.hasField(Reference._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, Tag> collectTags(FieldSet fields, List<FieldEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Tag.class.getSimpleName());

        Map<UUID, Tag> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Tag._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(FieldEntity::getTextListValue).flatMap(List::stream).map(UUID::fromString).distinct().collect(Collectors.toList()),
                    x -> {
                        Tag item = new Tag();
                        item.setId(x);
                        return item;
                    },
                    Tag::getId);
        } else {
            List<UUID> ids = new ArrayList<>();
            for (FieldEntity field: data) {
                if (field.getTextListValue() != null) {
                    for (String value: field.getTextListValue()) {
                        if (value != null && !ids.contains(UUID.fromString(value))) ids.add(UUID.fromString(value));
                    }
                }
            }
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Tag._id);
            TagQuery q = this.queryFactory.query(TagQuery.class).disableTracking().authorize(this.authorize).ids(ids);
            itemMap = this.builderFactory.builder(TagBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Tag::getId);
        }

        if (!fields.hasField(Tag._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }
}
