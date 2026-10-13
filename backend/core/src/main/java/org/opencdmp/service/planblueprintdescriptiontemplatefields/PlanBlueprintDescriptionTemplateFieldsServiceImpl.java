package org.opencdmp.service.planblueprintdescriptiontemplatefields;

import gr.cite.tools.logging.LoggerService;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.*;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.types.descriptiontemplate.*;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.*;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.descriptiontemplate.*;
import org.opencdmp.model.persist.descriptiontemplatedefinition.*;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.BaseFieldDataPersist;
import org.opencdmp.service.fielddatahelper.FieldDataHelperService;
import org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlanBlueprintDescriptionTemplateFieldsServiceImpl implements PlanBlueprintDescriptionTemplateFieldsService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintDescriptionTemplateFieldsServiceImpl.class));

    private final ConventionService conventionService;

    private final FieldDataHelperServiceProvider fieldDataHelperServiceProvider;

    @Autowired
    public PlanBlueprintDescriptionTemplateFieldsServiceImpl(ConventionService conventionService, FieldDataHelperServiceProvider fieldDataHelperServiceProvider) {
        this.conventionService = conventionService;
        this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
    }

    //region Persist

    @Override
    public @NotNull SectionEntity buildSectionEntity(SectionPersist persist) {
        SectionEntity data = new SectionEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setDescription(persist.getDescription());
        data.setOrdinal(persist.getOrdinal());
        data.setTitle(persist.getTitle());
        data.setSemantics(persist.getSemantics());

        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (SectionPersist sectionPersist : persist.getSections()) {
                data.getSections().add(this.buildSectionEntity(sectionPersist));
            }
        }

        if (!this.conventionService.isListNullOrEmpty(persist.getFieldSets())) {
            data.setFieldSets(new ArrayList<>());
            for (FieldSetPersist fieldSetPersist : persist.getFieldSets()) {
                data.getFieldSets().add(this.buildFieldSetEntity(fieldSetPersist));
            }
        }

        return data;
    }

    @Override
    public @NotNull List<FieldSetEntity> buildFieldSetEntities(List<FieldSetPersist> persists) {
        List<FieldSetEntity> result = new ArrayList<>();
        if (persists == null) return result;
        for (FieldSetPersist persist : persists) {
            result.add(this.buildFieldSetEntity(persist));
        }
        return result;
    }

    private @NotNull FieldSetEntity buildFieldSetEntity(FieldSetPersist persist) {
        FieldSetEntity data = new FieldSetEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setDescription(persist.getDescription());
        data.setOrdinal(persist.getOrdinal());
        data.setTitle(persist.getTitle());
        data.setHasMultiplicity(persist.getHasMultiplicity());
        data.setAdditionalInformation(persist.getAdditionalInformation());
        data.setExtendedDescription(persist.getExtendedDescription());
        data.setSemantics(persist.getSemantics());
        if (persist.getMultiplicity() != null && persist.getHasMultiplicity()) {
            data.setMultiplicity(this.buildMultiplicityEntity(persist.getMultiplicity()));
        }
        data.setHasCommentField(persist.getHasCommentField());

        if (!this.conventionService.isListNullOrEmpty(persist.getFields())) {
            data.setFields(new ArrayList<>());
            for (FieldPersist fieldPersist : persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }
        return data;
    }

    private @NotNull FieldEntity buildFieldEntity(FieldPersist persist) {
        FieldEntity data = new FieldEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setOrdinal(persist.getOrdinal());
        data.setSemantics(persist.getSemantics());
        data.setValidations(persist.getValidations());
        data.setIncludeInExport(persist.getIncludeInExport());
        if (persist.getData() != null)
            data.setData(this.buildFieldDataEntity(persist.getData()));

        data.setDefaultValue(this.buildDefaultValueEntity(persist.getDefaultValue(), data));
        if (!this.conventionService.isListNullOrEmpty(persist.getVisibilityRules())) {
            data.setVisibilityRules(new ArrayList<>());
            for (RulePersist fieldPersist : persist.getVisibilityRules()) {
                data.getVisibilityRules().add(this.buildRuleEntity(fieldPersist, data));
            }
        }
        return data;
    }

    private BaseFieldDataEntity buildFieldDataEntity(BaseFieldDataPersist persist) {
        if (persist == null)
            return null;
        return this.fieldDataHelperServiceProvider.get(persist.getFieldType()).applyPersist(persist);
    }

    private @NotNull RuleEntity buildRuleEntity(RulePersist persist, FieldEntity fieldEntity) {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        RuleEntity data = new RuleEntity();
        if (persist == null) return data;
        data.setTarget(persist.getTarget());

        if (FieldType.isTextType(fieldType) || FieldType.isTextListType(fieldType)) {
            if (FieldType.UPLOAD.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("Upload not supported");
            if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue()))  throw new NotImplementedException("plans not supported");
            if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("descriptions not supported");
            
            data.setTextValue(persist.getTextValue());
        }
        else if (FieldType.isReferenceType(fieldType) ) {
            throw new NotImplementedException("reference not supported");
        }  
        else if (FieldType.isTagType(fieldType) ) {
            throw new NotImplementedException("tags not supported");
        }
        else if (FieldType.isDateType(fieldType))  data.setDateValue(persist.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  data.setBooleanValue(persist.getBooleanValue());
        else if (FieldType.isNumberType(fieldType))  data.setNumberValue(persist.getNumberValue());
        else if (FieldType.isExternalIdentifierType(fieldType)) throw new NotImplementedException("ExternalIdentifier not supported");

        return data;
    }

    private @NotNull DefaultValueEntity buildDefaultValueEntity(DefaultValuePersist persist, FieldEntity fieldEntity)  {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        DefaultValueEntity data = new DefaultValueEntity();
        if (persist == null || persist.isNullOrEmpty()) return data;

        if (FieldType.isTextType(fieldType) || FieldType.isTextListType(fieldType)) {
            if (FieldType.UPLOAD.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("Upload not supported");
            if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue()))  throw new NotImplementedException("plans not supported");
            if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("descriptions not supported");

            data.setTextValue(persist.getTextValue());
        }
        else if (FieldType.isReferenceType(fieldType) ) {
            throw new NotImplementedException("reference not supported");
        }
        else if (FieldType.isTagType(fieldType) ) {
            throw new NotImplementedException("tags not supported");
        }
        else if (FieldType.isDateType(fieldType))  data.setDateValue(persist.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  data.setBooleanValue(persist.getBooleanValue());
        else if (FieldType.isNumberType(fieldType))  data.setNumberValue(persist.getNumberValue());
        else if (FieldType.isExternalIdentifierType(fieldType)){
            throw new NotImplementedException("external identifier not supported");
        }

        return data;
    }

    private @NotNull MultiplicityEntity buildMultiplicityEntity(MultiplicityPersist persist) {
        MultiplicityEntity data = new MultiplicityEntity();
        if (persist == null)
            return data;

        if (persist.getMax() != null)
            data.setMax(persist.getMax());
        if (persist.getMin() != null)
            data.setMin(persist.getMin());
        if (persist.getPlaceholder() != null)
            data.setPlaceholder(persist.getPlaceholder());
        if (persist.getTableView() != null)
            data.setTableView(persist.getTableView());
        if (persist.getTransposedTableView() != null)
            data.setTransposedTableView(persist.getTransposedTableView());
        return data;
    }

    //endregion

    //region Clone

    @Override
    public void reassignSection(Section model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;
        model.setId(UUID.randomUUID().toString());

        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                this.reassignSection(section, visibilityRulesMap);
            }
        }
        if (model.getFieldSets() != null) {
            for (org.opencdmp.model.descriptiontemplate.FieldSet fieldSet : model.getFieldSets()) {
                this.reassignFieldSet(fieldSet, visibilityRulesMap);
            }
        }
    }

    private void reassignFieldSet(org.opencdmp.model.descriptiontemplate.FieldSet model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;

        String oldFieldSetId = model.getId();
        if (visibilityRulesMap != null && visibilityRulesMap.containsKey(oldFieldSetId)){
            model.setId(UUID.randomUUID().toString());
            visibilityRulesMap.put(oldFieldSetId, model.getId());
        } else {
            model.setId(UUID.randomUUID().toString());
        }

        if (model.getFields() != null) {
            for (Field field : model.getFields()) {
                this.reassignField(field, visibilityRulesMap);
            }
        }
    }

    private void reassignField(Field model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;
        String oldFieldId = model.getId();
        if (visibilityRulesMap != null && visibilityRulesMap.containsKey(oldFieldId)){
            model.setId(UUID.randomUUID().toString());
            visibilityRulesMap.put(oldFieldId, model.getId());
        } else {
            model.setId(UUID.randomUUID().toString());
        }
    }

    //endregion

    //region Import
    @Override
    public SectionPersist xmlSectionToPersist(DescriptionTemplateSectionImportExport importExport) {
        SectionPersist sectionEntity = new SectionPersist();
        List<SectionPersist> sectionsListEntity = new LinkedList<>();

        if (!this.conventionService.isListNullOrEmpty(importExport.getSections())) {
            for (DescriptionTemplateSectionImportExport xmlSection : importExport.getSections()) {
                sectionsListEntity.add(this.xmlSectionToPersist(xmlSection));
            }
        }
        sectionEntity.setId(importExport.getId());
        sectionEntity.setOrdinal(importExport.getOrdinal());
        sectionEntity.setTitle(importExport.getTitle());
        sectionEntity.setDescription(importExport.getDescription());
        sectionEntity.setSemantics(importExport.getSemantics());
        List<FieldSetPersist> fieldSetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getFieldSets())) {
            for (DescriptionTemplateFieldSetImportExport xmlFieldSet : importExport.getFieldSets()) {
                fieldSetEntity.add(this.toFieldSetModel(xmlFieldSet));
            }
        }
        sectionEntity.setFieldSets(fieldSetEntity);

        sectionEntity.setSections(sectionsListEntity);
        return sectionEntity;
    }

    private FieldSetPersist toFieldSetModel(DescriptionTemplateFieldSetImportExport importExport) {
        FieldSetPersist fieldSet1Entity = new FieldSetPersist();
        fieldSet1Entity.setId(importExport.getId());
        fieldSet1Entity.setOrdinal(importExport.getOrdinal());
        fieldSet1Entity.setHasCommentField(importExport.getHasCommentField() != null ? importExport.getHasCommentField() : false);
        fieldSet1Entity.setMultiplicity(importExport.getMultiplicity() != null ? this.xmlMultiplicityToPersist(importExport.getMultiplicity()) : null);
        fieldSet1Entity.setHasMultiplicity(importExport.getHasMultiplicity());
        fieldSet1Entity.setTitle(importExport.getTitle());
        fieldSet1Entity.setDescription(importExport.getDescription());
        fieldSet1Entity.setExtendedDescription(importExport.getExtendedDescription());
        fieldSet1Entity.setAdditionalInformation(importExport.getAdditionalInformation());
        fieldSet1Entity.setSemantics(importExport.getSemantics());

        List<FieldPersist> fieldsEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getFields())) {
            for (DescriptionTemplateFieldImportExport xmlField : importExport.getFields()) {
                fieldsEntity.add(this.xmlFieldToPersist(xmlField));
            }
        }
        fieldSet1Entity.setFields(fieldsEntity);
        return fieldSet1Entity;
    }

    private FieldPersist xmlFieldToPersist(DescriptionTemplateFieldImportExport importExport) {
        FieldPersist fieldEntity = new FieldPersist();
        fieldEntity.setId(importExport.getId());
        fieldEntity.setOrdinal(importExport.getOrdinal());
        fieldEntity.setValidations(importExport.getValidations());
        fieldEntity.setIncludeInExport(importExport.getIncludeInExport());
        fieldEntity.setDefaultValue(this.toRuleModel(importExport.getDefaultValue()));
        List<RulePersist> rulePersists = new ArrayList<>();
        if (importExport.getVisibilityRules() != null) {
            for (DescriptionTemplateRuleImportExport xmlRule : importExport.getVisibilityRules()) {
                rulePersists.add(this.toRuleModel(xmlRule));
            }
        }
        fieldEntity.setVisibilityRules(rulePersists);
        FieldType fieldType = importExport.getData().getFieldType();

        if (importExport.getData() != null) {
            FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(fieldType);
            fieldEntity.setData(fieldDataHelperService.importExportMapDataToPersist(importExport.getData()));
        }
        fieldEntity.setSemantics(importExport.getSemantics());
        return fieldEntity;
    }

    private RulePersist toRuleModel(DescriptionTemplateRuleImportExport importExport) {
        RulePersist ruleEntity = new RulePersist();
        ruleEntity.setTarget(importExport.getTarget());
        ruleEntity.setDateValue(importExport.getDateValue());
        ruleEntity.setBooleanValue(importExport.getBooleanValue());
        ruleEntity.setNumberValue(importExport.getNumberValue());
        ruleEntity.setTextValue(importExport.getTextValue());
        return ruleEntity;
    }

    private DefaultValuePersist toRuleModel(DescriptionTemplateDefaultValueImportExport importExport) {
        if (importExport == null) return null;
        DefaultValuePersist ruleEntity = new DefaultValuePersist();
        ruleEntity.setDateValue(importExport.getDateValue());
        ruleEntity.setBooleanValue(importExport.getBooleanValue());
        ruleEntity.setTextValue(importExport.getTextValue());
        ruleEntity.setNumberValue(importExport.getNumberValue());
        return ruleEntity;
    }

    private MultiplicityPersist xmlMultiplicityToPersist(DescriptionTemplateMultiplicityImportExport importXml) {
        MultiplicityPersist multiplicityEntity = new MultiplicityPersist();
        multiplicityEntity.setMax(importXml.getMax());
        multiplicityEntity.setMin(importXml.getMin());
        multiplicityEntity.setPlaceholder(importXml.getPlaceholder());
        multiplicityEntity.setTableView(importXml.getTableView());
        multiplicityEntity.setTransposedTableView(importXml.isTransposedTableView());
        return multiplicityEntity;
    }

    //endregion

    //region Export

    @Override
    public DescriptionTemplateSectionImportExport sectionXmlToExport(SectionEntity entity) {
        DescriptionTemplateSectionImportExport xml = new DescriptionTemplateSectionImportExport();
        List<DescriptionTemplateSectionImportExport> sectionsListEntity = new LinkedList<>();

        if (!this.conventionService.isListNullOrEmpty(entity.getSections())) {
            for (SectionEntity xmlSection : entity.getSections()) {
                sectionsListEntity.add(this.sectionXmlToExport(xmlSection));
            }
        }
        xml.setSections(sectionsListEntity);

        xml.setId(entity.getId());
        xml.setOrdinal(entity.getOrdinal());
        xml.setTitle(entity.getTitle());
        xml.setDescription(entity.getDescription());
        xml.setSemantics(entity.getSemantics());
        List<DescriptionTemplateFieldSetImportExport> fieldSetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFieldSets())) {
            for (FieldSetEntity xmlFieldSet : entity.getFieldSets()) {
                fieldSetEntity.add(this.fieldSetXmlToExport(xmlFieldSet));
            }
        }
        xml.setFieldSets(fieldSetEntity);

        return xml;
    }

    private DescriptionTemplateFieldSetImportExport fieldSetXmlToExport(FieldSetEntity entity) {
        DescriptionTemplateFieldSetImportExport fieldSet1Entity = new DescriptionTemplateFieldSetImportExport();
        fieldSet1Entity.setId(entity.getId());
        fieldSet1Entity.setOrdinal(entity.getOrdinal());
        fieldSet1Entity.setHasCommentField(entity.getHasCommentField());
        fieldSet1Entity.setHasMultiplicity(entity.getHasMultiplicity());
        fieldSet1Entity.setMultiplicity(entity.getMultiplicity() != null ? this.multiplicityXmlToExport(entity.getMultiplicity()) : null);
        fieldSet1Entity.setTitle(entity.getTitle());
        fieldSet1Entity.setDescription(entity.getDescription());
        fieldSet1Entity.setExtendedDescription(entity.getExtendedDescription());
        fieldSet1Entity.setAdditionalInformation(entity.getAdditionalInformation());
        fieldSet1Entity.setSemantics(entity.getSemantics());

        List<DescriptionTemplateFieldImportExport> fieldsEntity = new LinkedList<>();
        if (entity.getFields() != null) {
            for (FieldEntity xmlField : entity.getFields()) {
                fieldsEntity.add(this.fieldXmlToExport(xmlField));
            }
        }
        fieldSet1Entity.setFields(fieldsEntity);
        return fieldSet1Entity;
    }

    private DescriptionTemplateFieldImportExport fieldXmlToExport(FieldEntity entity) {
        DescriptionTemplateFieldImportExport xml = new DescriptionTemplateFieldImportExport();
        xml.setId(entity.getId());
        xml.setOrdinal(entity.getOrdinal());
        xml.setValidations(entity.getValidations());
        xml.setDefaultValue(this.toDefaultValueModel(entity.getDefaultValue()));
        xml.setIncludeInExport(entity.getIncludeInExport());
        List<DescriptionTemplateRuleImportExport> rulePersists = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getVisibilityRules())) {
            for (RuleEntity xmlRule : entity.getVisibilityRules()) {
                rulePersists.add(this.toRuleModel(xmlRule));
            }
        }
        xml.setVisibilityRules(rulePersists);

        if (entity.getData() != null) {
            FieldType fieldType = entity.getData().getFieldType();
            FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(fieldType);
            xml.setData(fieldDataHelperService.dataToImportExportXml(entity.getData()));
        }
        xml.setSemantics(entity.getSemantics());
        return xml;
    }

    private DescriptionTemplateDefaultValueImportExport toDefaultValueModel(DefaultValueEntity entity) {
        DescriptionTemplateDefaultValueImportExport xml = new DescriptionTemplateDefaultValueImportExport();

        if (entity == null) return xml;

        xml.setDateValue(entity.getDateValue());
        xml.setBooleanValue(entity.getBooleanValue());
        xml.setTextValue(entity.getTextValue());
        xml.setNumberValue(entity.getNumberValue());
        return xml;
    }

    private DescriptionTemplateRuleImportExport toRuleModel(RuleEntity entity) {
        DescriptionTemplateRuleImportExport xml = new DescriptionTemplateRuleImportExport();

        if (entity == null) return xml;

        xml.setTarget(entity.getTarget());
        xml.setDateValue(entity.getDateValue());
        xml.setBooleanValue(entity.getBooleanValue());
        xml.setTextValue(entity.getTextValue());
        xml.setNumberValue(entity.getNumberValue());
        return xml;
    }

    private DescriptionTemplateMultiplicityImportExport multiplicityXmlToExport(MultiplicityEntity entity) {
        DescriptionTemplateMultiplicityImportExport xml = new DescriptionTemplateMultiplicityImportExport();

        if (entity == null) return xml;

        xml.setMax(entity.getMax());
        xml.setMin(entity.getMin());
        xml.setPlaceholder(entity.getPlaceholder());
        xml.setTableView(entity.getTableView());
        xml.setTransposedTableView(entity.isTransposedTableView());
        return xml;
    }

    //endregion

    //region Import Common Model 

    @Override
    public SectionPersist commonModelSectionToPersist(SectionModel commonModel) {
        SectionPersist sectionEntity = new SectionPersist();
        List<SectionPersist> sectionsListEntity = new LinkedList<>();

        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            for (SectionModel sectionModel : commonModel.getSections()) {
                sectionsListEntity.add(this.commonModelSectionToPersist(sectionModel));
            }
        }
        sectionEntity.setId(commonModel.getId());
        sectionEntity.setOrdinal(commonModel.getOrdinal());
        sectionEntity.setTitle(commonModel.getTitle());
        sectionEntity.setDescription(commonModel.getDescription());
        sectionEntity.setSemantics(commonModel.getSemantics());
        List<FieldSetPersist> fieldSetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getFieldSets())) {
            for (FieldSetModel fieldSetModel : commonModel.getFieldSets()) {
                fieldSetEntity.add(this.commonModelToFieldSetModel(fieldSetModel));
            }
        }
        sectionEntity.setFieldSets(fieldSetEntity);

        sectionEntity.setSections(sectionsListEntity);
        return sectionEntity;
    }

    @Override
    public FieldSetPersist commonModelToFieldSetModel(FieldSetModel commonModel) {
        FieldSetPersist fieldSet1Entity = new FieldSetPersist();
        fieldSet1Entity.setId(commonModel.getId());
        fieldSet1Entity.setOrdinal(commonModel.getOrdinal());
        fieldSet1Entity.setMultiplicity(commonModel.getMultiplicity() != null ? this.commonModelMultiplicityToPersist(commonModel.getMultiplicity()) : null);
        fieldSet1Entity.setHasMultiplicity(commonModel.getMultiplicity() != null);
        fieldSet1Entity.setTitle(commonModel.getTitle());
        fieldSet1Entity.setDescription(commonModel.getDescription());
        fieldSet1Entity.setExtendedDescription(commonModel.getExtendedDescription());
        fieldSet1Entity.setAdditionalInformation(commonModel.getAdditionalInformation());
        fieldSet1Entity.setSemantics(commonModel.getSemantics());

        List<FieldPersist> fieldsEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getFields())) {
            for (FieldModel fieldModel : commonModel.getFields()) {
                fieldsEntity.add(this.commonModelFieldToPersist(fieldModel));
            }
        }
        fieldSet1Entity.setFields(fieldsEntity);
        return fieldSet1Entity;
    }

    private FieldPersist commonModelFieldToPersist(FieldModel commonModel) {
        FieldPersist fieldEntity = new FieldPersist();
        fieldEntity.setId(commonModel.getId());
        fieldEntity.setOrdinal(commonModel.getOrdinal());
        fieldEntity.setValidations(this.commonModelValidationsToPersist(commonModel.getValidations()));
        fieldEntity.setIncludeInExport(commonModel.getIncludeInExport());
        fieldEntity.setDefaultValue(this.commonModelRuleToPersist(commonModel.getDefaultValue()));
        List<RulePersist> rulePersists = new ArrayList<>();
        fieldEntity.setVisibilityRules(rulePersists);
        FieldType fieldType;
        switch (commonModel.getData().getFieldType()){
            case SELECT -> fieldType = FieldType.SELECT;
            case BOOLEAN_DECISION -> fieldType = FieldType.BOOLEAN_DECISION;
            case RADIO_BOX -> fieldType = FieldType.RADIO_BOX;
            case INTERNAL_ENTRIES_PlANS -> fieldType = FieldType.INTERNAL_ENTRIES_PLANS;
            case INTERNAL_ENTRIES_DESCRIPTIONS -> fieldType = FieldType.INTERNAL_ENTRIES_DESCRIPTIONS;
            case CHECK_BOX -> fieldType = FieldType.CHECK_BOX;
            case FREE_TEXT -> fieldType = FieldType.FREE_TEXT;
            case TEXT_AREA -> fieldType = FieldType.TEXT_AREA;
            case RICH_TEXT_AREA -> fieldType = FieldType.RICH_TEXT_AREA;
            case UPLOAD -> fieldType = FieldType.UPLOAD;
            case DATE_PICKER -> fieldType = FieldType.DATE_PICKER;
            case TAGS -> fieldType = FieldType.TAGS;
            case REFERENCE_TYPES -> fieldType = FieldType.REFERENCE_TYPES;
            case DATASET_IDENTIFIER -> fieldType = FieldType.DATASET_IDENTIFIER;
            case VALIDATION -> fieldType = FieldType.VALIDATION;
            case SYSTEM -> fieldType = FieldType.SYSTEM;
            case NUMBER -> fieldType = FieldType.NUMBER;
            default -> throw new InternalError("unknown type: " + commonModel.getData().getFieldType());
        }

        if (commonModel.getData() != null) {
            FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(fieldType);
            fieldEntity.setData(fieldDataHelperService.commonModelMapDataToPersist(commonModel.getData()));
        }
        fieldEntity.setSemantics(commonModel.getSemantics());
        return fieldEntity;
    }
    
    private List<FieldValidationType> commonModelValidationsToPersist(List<org.opencdmp.commonmodels.enums.FieldValidationType> commonModel) {
        if (this.conventionService.isListNullOrEmpty(commonModel)) return null;
        List<FieldValidationType> validationTypes = new ArrayList<>();
        for (org.opencdmp.commonmodels.enums.FieldValidationType fieldValidationType : commonModel){
            switch (fieldValidationType){
                case Url -> validationTypes.add(FieldValidationType.Url);
                case Required -> validationTypes.add(FieldValidationType.Required);
                case None -> validationTypes.add(FieldValidationType.None);
                case Email -> validationTypes.add(FieldValidationType.Email);
                default -> throw new InternalError("unknown type: " + fieldValidationType);
            }
        }
        return validationTypes;
    }

    private DefaultValuePersist commonModelRuleToPersist(DefaultValueModel commonModel) {
        if (commonModel == null) return null;
        DefaultValuePersist ruleEntity = new DefaultValuePersist();
        ruleEntity.setDateValue(commonModel.getDateValue());
        ruleEntity.setBooleanValue(commonModel.getBooleanValue());
        ruleEntity.setNumberValue(commonModel.getNumberValue());
        ruleEntity.setTextValue(commonModel.getTextValue());
        return ruleEntity;
    }

    private MultiplicityPersist commonModelMultiplicityToPersist(MultiplicityModel commonModel) {
        MultiplicityPersist multiplicityEntity = new MultiplicityPersist();
        multiplicityEntity.setMax(commonModel.getMax());
        multiplicityEntity.setMin(commonModel.getMin());
        multiplicityEntity.setPlaceholder(commonModel.getPlaceholder());
        multiplicityEntity.setTableView(commonModel.getTableView());
        multiplicityEntity.setTransposedTableView(commonModel.getTransposedTableView());
        return multiplicityEntity;
    }

    //endregion
}

