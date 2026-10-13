package org.opencdmp.service.plandescriptionfields;


import org.jetbrains.annotations.NotNull;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.model.persist.descriptionproperties.FieldPersist;
import org.opencdmp.commons.types.description.importexport.DescriptionPropertyDefinitionFieldSetImportExport;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateSectionImportExport;
import org.opencdmp.commons.types.reference.importexport.ReferenceImportExport;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.TagEntity;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionFieldSetPersist;
import org.opencdmp.service.visibility.VisibilityService;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlanDescriptionFieldsService {


    @NotNull
    PropertyDefinitionFieldSetEntity cleanPropertyDefinitionFieldSetEntity(PropertyDefinitionFieldSetEntity persist, FieldSetEntity fieldSetEntity);

    @NotNull PropertyDefinitionFieldSetEntity buildPropertyDefinitionFieldSetEntity(PropertyDefinitionFieldSetPersist persist, FieldSetEntity fieldSetEntity, Map<String, List<UUID>> fieldToReferenceMap, VisibilityService visibilityService) throws InvalidApplicationException;

    @NotNull PropertyDefinitionFieldSetPersist buildPropertyDefinitionFieldSetPersist(PropertyDefinitionFieldSetEntity data, FieldSetEntity fieldSetEntity, List<ReferenceEntity> references);

    DescriptionPropertyDefinitionFieldSetImportExport descriptionPropertyDefinitionFieldSetToExport(PropertyDefinitionFieldSetEntity entity, String fieldSetId, List<? extends SectionEntity> sections, Map<UUID, TagEntity> tagEntityMap);

    PropertyDefinitionFieldSetPersist xmlPropertyDefinitionFieldSetToPersist(DescriptionPropertyDefinitionFieldSetImportExport importXml, List<ReferenceImportExport> references, List<? extends DescriptionTemplateSectionImportExport> sections);

    PropertyDefinitionFieldSetPersist commonModelPropertyDefinitionFieldSetToPersist(PropertyDefinitionFieldSetModel commonModel, List<? extends SectionModel> sections);

    @NotNull FieldPersist buildFieldPersist(FieldEntity data, org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity, List<ReferenceEntity> references);

    FieldPersist commonModelFieldToPersist(FieldModel model, List<? extends SectionModel> sections);
}
