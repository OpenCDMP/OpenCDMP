package org.opencdmp.service.planblueprintdescriptiontemplatefields;

import org.jetbrains.annotations.NotNull;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateSectionImportExport;
import org.opencdmp.model.descriptiontemplate.Section;
import org.opencdmp.model.persist.descriptiontemplatedefinition.FieldSetPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.SectionPersist;

import java.util.List;
import java.util.Map;

public interface PlanBlueprintDescriptionTemplateFieldsService {

    @NotNull
    SectionEntity buildSectionEntity(SectionPersist persist);

    @NotNull
    List<FieldSetEntity> buildFieldSetEntities(List<FieldSetPersist> persists);

    void reassignSection(Section model, Map<String, String> visibilityRulesMap);

    //region Import
    SectionPersist xmlSectionToPersist(DescriptionTemplateSectionImportExport importExport);

    DescriptionTemplateSectionImportExport sectionXmlToExport(SectionEntity entity);

    SectionPersist commonModelSectionToPersist(SectionModel commonModel);

    FieldSetPersist commonModelToFieldSetModel(FieldSetModel commonModel);
}
