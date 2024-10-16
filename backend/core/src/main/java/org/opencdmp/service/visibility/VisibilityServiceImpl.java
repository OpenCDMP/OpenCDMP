package org.opencdmp.service.visibility;

import org.apache.commons.lang3.NotImplementedException;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.*;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionPersist;

import java.util.*;

public class VisibilityServiceImpl implements VisibilityService {

	private final DefinitionEntity definition;
	private final PropertyDefinition propertyDefinition;

	private Map<String, List<RuleWithTarget>> rulesBySources;
	private Map<String, List<RuleWithTarget>> rulesByTarget;
	private Map<FieldKey, Boolean> visibility;
	
    public VisibilityServiceImpl(DefinitionEntity definition, PropertyDefinitionPersist propertyDefinition) {
	    this.definition = definition;
	    this.propertyDefinition = new PropertyDefinition(propertyDefinition);
    }

	public VisibilityServiceImpl(DefinitionEntity definition, PropertyDefinitionEntity propertyDefinition) {
		this.definition = definition;
		this.propertyDefinition = new PropertyDefinition(propertyDefinition);
	}

	private void initRules(){
		if (this.rulesBySources != null && this.rulesByTarget != null) return;
		this.rulesBySources = new HashMap<>();
		this.rulesByTarget = new HashMap<>();
		for (FieldEntity fieldEntity : this.definition.getAllField()){
			if (fieldEntity.getVisibilityRules() != null && !fieldEntity.getVisibilityRules().isEmpty()) {
				for (RuleEntity rule : fieldEntity.getVisibilityRules()){
					if (!this.rulesBySources.containsKey(fieldEntity.getId())) this.rulesBySources.put(fieldEntity.getId(), new ArrayList<>());
					RuleWithTarget ruleWithTarget = new RuleWithTarget(fieldEntity.getId(), rule, fieldEntity);
					this.rulesBySources.get(fieldEntity.getId()).add(ruleWithTarget);

					if (!this.rulesByTarget.containsKey(rule.getTarget())) this.rulesByTarget.put(rule.getTarget(), new ArrayList<>());
					this.rulesByTarget.get(rule.getTarget()).add(ruleWithTarget);
				}
			}
		}
	}

	@Override
	public boolean isVisible(String id, Integer ordinal) {
		this.calculateVisibility();
		FieldKey fieldKey = new FieldKey(id, ordinal);
		return this.visibility.getOrDefault(fieldKey, false);
	}

	@Override
	public Map<FieldKey, Boolean> getVisibilityStates() {
		this.calculateVisibility();
		return this.visibility;
	}
	
	private void calculateVisibility(){
		if (this.visibility != null) return;
		
		this.initRules();
		this.buildTargetVisibility();
		this.expandVisibilityToChildren();
		this.setDefaultVisibilityForNotCaclucted();
		this.hideParentIfAllChildrenAreHidden();
	}
	
	private void buildTargetVisibility(){
		this.visibility = new HashMap<>();
		for (Map.Entry<String, List<RuleWithTarget>> ruleForSource : this.rulesBySources.entrySet()){
			for (RuleWithTarget rule : ruleForSource.getValue()){
				if (this.propertyDefinition.getFieldSets() != null && !this.propertyDefinition.getFieldSets().isEmpty()){
					for (PropertyDefinitionFieldSet propertyDefinitionFieldSet: this.propertyDefinition.getFieldSets().values()) {
						if (propertyDefinitionFieldSet.getItems() != null && !propertyDefinitionFieldSet.getItems().isEmpty()) {
							for (PropertyDefinitionFieldSetItem definitionFieldSetItem : propertyDefinitionFieldSet.getItems()) {
								if (definitionFieldSetItem.getFields() != null && !definitionFieldSetItem.getFields().isEmpty()) {
									for (String key : definitionFieldSetItem.getFields().keySet()) {
										if (rule.getSource().equals(key)){
											Field field = definitionFieldSetItem.getFields().get(key);

											List<RuleWithTarget> rulesForParentKey = this.getChainParentRules(rule);

											boolean parentIsVisible = rulesForParentKey != null && !rulesForParentKey.isEmpty() ? this.isChainParentVisible(rulesForParentKey, definitionFieldSetItem.getFields(), definitionFieldSetItem.getOrdinal()) : true;
											if (definitionFieldSetItem.getFields().containsKey(rule.getTarget())){ //Rule applies only for current multiple item
												FieldKey fieldKey = new FieldKey(rule.getTarget(), definitionFieldSetItem.getOrdinal());
												boolean currentState = this.visibility.getOrDefault(fieldKey, false);
												this.visibility.put(fieldKey, parentIsVisible && (currentState || this.ruleIsTrue(rule, field)));
											} else if (!this.definition.getFieldById(rule.getTarget()).isEmpty() || !this.definition.getFieldSetById(rule.getTarget()).isEmpty()) { //Rule applies to different fieldset, so we apply for all multiple items
												List<Integer> ordinals = this.getKeyOrdinals(rule.getTarget());
												for (Integer ordinal : ordinals){
													FieldKey fieldKey = new FieldKey(rule.getTarget(), ordinal);
													boolean currentState = this.visibility.getOrDefault(fieldKey, false);
													this.visibility.put(fieldKey, parentIsVisible && (currentState || this.ruleIsTrue(rule, field)));
												}
											} else {
												FieldKey fieldKey = new FieldKey(rule.getTarget(), null); //Ordinal is null if target not on field
												boolean currentState = this.visibility.getOrDefault(fieldKey, false);
												this.visibility.put(fieldKey, parentIsVisible && (currentState || this.ruleIsTrue(rule, field)));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean isChainParentVisible(List<RuleWithTarget> rulesForParentKey,  Map<String, Field> fieldsMap, int ordinal) {
		boolean isVisible = false;
		if (rulesForParentKey == null || rulesForParentKey.isEmpty()) return false;

		for (RuleWithTarget ruleForParentKey : rulesForParentKey) {
			Field field = fieldsMap.get(ruleForParentKey.getSource());

			List<RuleWithTarget> rulesForGrandParentKey = this.getChainParentRules(ruleForParentKey);

			if (fieldsMap.containsKey(ruleForParentKey.getTarget())){ //Rule applies only for current multiple item
				FieldKey fieldKey = new FieldKey(ruleForParentKey.getTarget(), ordinal);
				boolean currentState = this.visibility.getOrDefault(fieldKey, false);
				isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
				if (rulesForGrandParentKey != null && !rulesForGrandParentKey.isEmpty()) isVisible = isVisible || this.isChainParentVisible(rulesForGrandParentKey, fieldsMap, ordinal);

			} else if (!this.definition.getFieldById(ruleForParentKey.getTarget()).isEmpty() || !this.definition.getFieldSetById(ruleForParentKey.getTarget()).isEmpty()) { //Rule applies to different fieldset, so we apply for all multiple items
				List<Integer> ordinals = this.getKeyOrdinals(ruleForParentKey.getTarget());
				for (Integer curentOrdinal : ordinals){
					FieldKey fieldKey = new FieldKey(ruleForParentKey.getTarget(), curentOrdinal);
					boolean currentState = this.visibility.getOrDefault(fieldKey, false);
					isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
					if (rulesForGrandParentKey != null && !rulesForGrandParentKey.isEmpty()) isVisible = isVisible || this.isChainParentVisible(rulesForGrandParentKey, this.getKeyFields(ruleForParentKey.getTarget(), curentOrdinal), ordinal);
				}
			} else {
				FieldKey fieldKey = new FieldKey(ruleForParentKey.getTarget(), null); //Ordinal is null if target not on field
				boolean currentState = this.visibility.getOrDefault(fieldKey, false);
				isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
				//Nothing to check for grandfather this type of field can not have rules
			}

			if (isVisible) break;
		}
		return isVisible;
	}

	private List<RuleWithTarget> getChainParentRules(RuleWithTarget rule) {
		if (rule == null || rule.getSource() == null || rule.getSource().isBlank() || this.rulesByTarget == null) return null;
		return  this.rulesByTarget.containsKey(rule.getSource()) ? this.rulesByTarget.get(rule.getSource()).stream().filter(Objects::nonNull).toList() : new ArrayList<>();
	}

	private Map<String, Field> getKeyFields(String key, int ordinal){
		if (this.propertyDefinition.getFieldSets() != null && !this.propertyDefinition.getFieldSets().isEmpty()){
			for (Map.Entry<String, PropertyDefinitionFieldSet> propertyDefinitionFieldSet: this.propertyDefinition.getFieldSets().entrySet()) {
				if (propertyDefinitionFieldSet.getKey().equals(key)) return propertyDefinitionFieldSet.getValue().getItems().stream().filter(x-> x.getOrdinal() == ordinal).map(PropertyDefinitionFieldSetItem::getFields).findFirst().orElse(new HashMap<>());

				if (propertyDefinitionFieldSet.getValue() != null && propertyDefinitionFieldSet.getValue().getItems() != null && !propertyDefinitionFieldSet.getValue().getItems().isEmpty()) {
					for (PropertyDefinitionFieldSetItem definitionFieldSetItem : propertyDefinitionFieldSet.getValue().getItems()) {
						if (definitionFieldSetItem.getFields() != null && !definitionFieldSetItem.getFields().isEmpty()) {
							for (String fieldKey : definitionFieldSetItem.getFields().keySet()) {
								if (fieldKey.equals(key)) return propertyDefinitionFieldSet.getValue().getItems().stream().filter(x-> x.getOrdinal() == ordinal).map(PropertyDefinitionFieldSetItem::getFields).findFirst().orElse(new HashMap<>());
							}
						}
					}
				}
			}
		}
		return new HashMap<>();
	}
	
	private List<Integer> getKeyOrdinals(String key){
		if (this.propertyDefinition.getFieldSets() != null && !this.propertyDefinition.getFieldSets().isEmpty()){
			for (Map.Entry<String, PropertyDefinitionFieldSet> propertyDefinitionFieldSet: this.propertyDefinition.getFieldSets().entrySet()) {
				if (propertyDefinitionFieldSet.getKey().equals(key)) return propertyDefinitionFieldSet.getValue().getItems().stream().map(PropertyDefinitionFieldSetItem::getOrdinal).toList();
				
				if (propertyDefinitionFieldSet.getValue() != null && propertyDefinitionFieldSet.getValue().getItems() != null && !propertyDefinitionFieldSet.getValue().getItems().isEmpty()) {
					for (PropertyDefinitionFieldSetItem definitionFieldSetItem : propertyDefinitionFieldSet.getValue().getItems()) {
						if (definitionFieldSetItem.getFields() != null && !definitionFieldSetItem.getFields().isEmpty()) {
							for (String fieldKey : definitionFieldSetItem.getFields().keySet()) {
								if (fieldKey.equals(key)) return propertyDefinitionFieldSet.getValue().getItems().stream().map(PropertyDefinitionFieldSetItem::getOrdinal).toList();
							}
						}
					}
				}
			}
		}
		return new ArrayList<>();
	}
	
	private void expandVisibilityToChildren(){
		if (this.definition.getPages() == null) return;
		for (PageEntity pageEntity : this.definition.getPages()){
			FieldKey fieldKey = new FieldKey(pageEntity.getId(), null);
			Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
			this.expandPageVisibility(pageEntity, currentValue);
		}
	}
	
	private void expandPageVisibility(PageEntity pageEntity, Boolean parentVisibility){
		if (pageEntity.getSections() == null) return;
		for (SectionEntity sectionEntity : pageEntity.getSections()){
			FieldKey fieldKey = new FieldKey(sectionEntity.getId(), null);
			Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
			if (currentValue != null){
				if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
					this.visibility.put(fieldKey, false);
					this.expandSectionVisibility(sectionEntity, currentValue);
				} else {
					this.expandSectionVisibility(sectionEntity, currentValue);
				}
			} else {
				if (parentVisibility != null) this.visibility.put(fieldKey, parentVisibility);
				this.expandSectionVisibility(sectionEntity, parentVisibility);
			}
		}
	}
	private void expandSectionVisibility(SectionEntity sectionEntity, Boolean parentVisibility){
		if (sectionEntity.getSections() != null) {
			for (SectionEntity subSectionEntity : sectionEntity.getSections()) {
				FieldKey fieldKey = new FieldKey(subSectionEntity.getId(), null);
				Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
				if (currentValue != null){
					if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
						this.visibility.put(fieldKey, false);
						this.expandSectionVisibility(subSectionEntity, currentValue);
					} else {
						this.expandSectionVisibility(subSectionEntity, currentValue);
					}
				} else {
					if (parentVisibility != null) this.visibility.put(fieldKey, parentVisibility);
					this.expandSectionVisibility(subSectionEntity, parentVisibility);
				}
			}
		}
		if (sectionEntity.getFieldSets() != null) {
			for (FieldSetEntity fieldSetEntity : sectionEntity.getFieldSets()) {
				if (this.propertyDefinition.getFieldSets() != null && !this.propertyDefinition.getFieldSets().isEmpty()){
					PropertyDefinitionFieldSet propertyDefinitionFieldSet = this.propertyDefinition.getFieldSets().getOrDefault(fieldSetEntity.getId(), null);
					if (propertyDefinitionFieldSet != null && propertyDefinitionFieldSet.getItems() != null && !propertyDefinitionFieldSet.getItems().isEmpty()) {
						for (PropertyDefinitionFieldSetItem definitionFieldSetItem : propertyDefinitionFieldSet.getItems()) {
							FieldKey fieldKey = new FieldKey(fieldSetEntity.getId(), definitionFieldSetItem.getOrdinal());
							Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
							if (currentValue != null){
								if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
									this.visibility.put(fieldKey, false);
									this.expandFieldSetVisibility(fieldSetEntity, currentValue, definitionFieldSetItem.getOrdinal());
								} else {
									this.expandFieldSetVisibility(fieldSetEntity, currentValue, definitionFieldSetItem.getOrdinal());
								}
							} else {
								if (parentVisibility != null) this.visibility.put(fieldKey, parentVisibility);
								this.expandFieldSetVisibility(fieldSetEntity, parentVisibility, definitionFieldSetItem.getOrdinal());
							}
						}
					}
				}
			}
		}
	}
	
	private void expandFieldSetVisibility(FieldSetEntity fieldSetEntity, Boolean parentVisibility, Integer ordinal){
		if (fieldSetEntity.getFields() != null) {
			for (FieldEntity fieldEntity : fieldSetEntity.getFields()) {
				FieldKey fieldKey = new FieldKey(fieldEntity.getId(), ordinal);
				Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
				if (currentValue != null){
					if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
						this.visibility.put(fieldKey, false);
					}
				} else if (parentVisibility != null){
					this.visibility.put(fieldKey, parentVisibility);
				}
			}
		}
	}


	private void setDefaultVisibilityForNotCaclucted(){
		if (this.definition.getPages() == null) return;
		for (PageEntity pageEntity : this.definition.getPages()){
			FieldKey fieldKey = new FieldKey(pageEntity.getId(), null);
			Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
			if (currentValue == null) this.visibility.put(fieldKey, true);
			this.setDefaultPageVisibility(pageEntity);
		}
	}

	private void setDefaultPageVisibility(PageEntity pageEntity){
		if (pageEntity.getSections() == null) return;
		for (SectionEntity sectionEntity : pageEntity.getSections()){
			FieldKey fieldKey = new FieldKey(sectionEntity.getId(), null);
			Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
			if (currentValue == null){
				this.visibility.put(fieldKey, true);
				this.setDefaultSectionVisibility(sectionEntity);
			}
		}
	}
	
	private void setDefaultSectionVisibility(SectionEntity sectionEntity){
		if (sectionEntity.getSections() != null) {
			for (SectionEntity subSectionEntity : sectionEntity.getSections()) {
				FieldKey fieldKey = new FieldKey(subSectionEntity.getId(), null);
				Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
				if (currentValue == null){
					this.visibility.put(fieldKey, true);
					this.setDefaultSectionVisibility(subSectionEntity);
				}
			}
		}
		if (sectionEntity.getFieldSets() != null) {
			for (FieldSetEntity fieldSetEntity : sectionEntity.getFieldSets()) {
				if (this.propertyDefinition.getFieldSets() != null && !this.propertyDefinition.getFieldSets().isEmpty()){
					PropertyDefinitionFieldSet propertyDefinitionFieldSet = this.propertyDefinition.getFieldSets().getOrDefault(fieldSetEntity.getId(), null);
					if (propertyDefinitionFieldSet != null && propertyDefinitionFieldSet.getItems() != null && !propertyDefinitionFieldSet.getItems().isEmpty()) {
						for (PropertyDefinitionFieldSetItem definitionFieldSetItem : propertyDefinitionFieldSet.getItems()) {
							FieldKey fieldKey = new FieldKey(fieldSetEntity.getId(), definitionFieldSetItem.getOrdinal());
							Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
							if (currentValue == null){
								this.visibility.put(fieldKey, true);
								this.setDefaultFieldSetVisibility(fieldSetEntity, definitionFieldSetItem.getOrdinal());
							}
						}
					}
				}
			}
		}
	}

	private void setDefaultFieldSetVisibility(FieldSetEntity fieldSetEntity, Integer ordinal){
		if (fieldSetEntity.getFields() != null) {
			for (FieldEntity fieldEntity : fieldSetEntity.getFields()) {
				FieldKey fieldKey = new FieldKey(fieldEntity.getId(), ordinal);
				Boolean currentValue = this.visibility.getOrDefault(fieldKey, null);
				if (currentValue == null){
					this.visibility.put(fieldKey, true);
				}
			}
		}
	}

	private void hideParentIfAllChildrenAreHidden(){
		if (this.definition.getPages() == null) return;
		for (PageEntity pageEntity : this.definition.getPages()){
			FieldKey fieldKey = new FieldKey(pageEntity.getId(), null);
			boolean isCurrentHidden = this.isHiddenPageVisibilityIfAllChildrenIsHidden(pageEntity);
			if (this.visibility.getOrDefault(fieldKey, true) && isCurrentHidden){
				this.visibility.put(fieldKey, false);
			}
		}
	}

	private boolean isHiddenPageVisibilityIfAllChildrenIsHidden(PageEntity pageEntity){
		boolean isHidden = true;
		if (pageEntity.getSections() == null) return isHidden;
		
		for (SectionEntity sectionEntity : pageEntity.getSections()){
			FieldKey fieldKey = new FieldKey(sectionEntity.getId(), null);
			boolean isCurrentHidden = this.isHiddenSectionIfAllChildrenIsHidden(sectionEntity);
			if (this.visibility.getOrDefault(fieldKey, true) && isCurrentHidden){
				this.visibility.put(fieldKey, false);
			}
			isHidden = isHidden && isCurrentHidden;
		}
		return isHidden;
	}

	private boolean isHiddenSectionIfAllChildrenIsHidden(SectionEntity sectionEntity){
		boolean isHidden = true;
		if (sectionEntity.getSections() != null) {
			for (SectionEntity subSectionEntity : sectionEntity.getSections()) {
				FieldKey fieldKey = new FieldKey(subSectionEntity.getId(), null);
				boolean isCurrentHidden = this.isHiddenSectionIfAllChildrenIsHidden(subSectionEntity);
				if (this.visibility.getOrDefault(fieldKey, true) && isCurrentHidden){
					this.visibility.put(fieldKey, false);
				}
				isHidden = isHidden && isCurrentHidden;
			}
		}
		if (sectionEntity.getFieldSets() != null) {
			for (FieldSetEntity fieldSetEntity : sectionEntity.getFieldSets()) {
				if (this.propertyDefinition.getFieldSets() != null && !this.propertyDefinition.getFieldSets().isEmpty()){
					PropertyDefinitionFieldSet propertyDefinitionFieldSet = this.propertyDefinition.getFieldSets().getOrDefault(fieldSetEntity.getId(), null);
					if (propertyDefinitionFieldSet != null && propertyDefinitionFieldSet.getItems() != null && !propertyDefinitionFieldSet.getItems().isEmpty()) {
						for (PropertyDefinitionFieldSetItem definitionFieldSetItem : propertyDefinitionFieldSet.getItems()) {
							FieldKey fieldKey = new FieldKey(fieldSetEntity.getId(), definitionFieldSetItem.getOrdinal());
							boolean isCurrentHidden = this.isHiddenFieldSetIfAllChildrenIsHidden(fieldSetEntity, definitionFieldSetItem.getOrdinal());
							if (this.visibility.getOrDefault(fieldKey, true) && isCurrentHidden){
								this.visibility.put(fieldKey, false);
							}
							isHidden = isHidden && isCurrentHidden;
						}
					}
				}
			}
		}
		return isHidden;
	}

	private boolean isHiddenFieldSetIfAllChildrenIsHidden(FieldSetEntity fieldSetEntity, Integer ordinal){
		boolean isHidden = true;
		if (fieldSetEntity.getFields() != null) {
			for (FieldEntity fieldEntity : fieldSetEntity.getFields()) {
				FieldKey fieldKey = new FieldKey(fieldEntity.getId(), ordinal);
				Boolean currentValue = this.visibility.getOrDefault(fieldKey, true);
				isHidden = isHidden && !currentValue;
			}
			return isHidden;
		}
		return true;
	}
	
	private boolean ruleIsTrue(RuleWithTarget rule, Field field){
		if (field != null){
			org.opencdmp.commons.enums.FieldType fieldType = rule.getFieldEntity() != null && rule.getFieldEntity().getData() != null ? rule.getFieldEntity().getData().getFieldType() :  org.opencdmp.commons.enums.FieldType.FREE_TEXT;
			if ((org.opencdmp.commons.enums.FieldType.isTextType(fieldType) || org.opencdmp.commons.enums.FieldType.isTextListType(fieldType)) && field.getTextValue() != null && !field.getTextValue().isBlank()) {
				if (org.opencdmp.commons.enums.FieldType.UPLOAD.equals(fieldType)) throw new NotImplementedException("Upload file rule not supported");
				return field.getTextValue().equals(rule.getTextValue());
			}
			else if (rule.getTextValue() != null &&org.opencdmp.commons.enums.FieldType.isTextListType(fieldType) && field.getTextListValue() != null && !field.getTextListValue().isEmpty()) {
				if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType))  throw new NotImplementedException("plans not supported");
				if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType)) throw new NotImplementedException("descriptions not supported");

				return new HashSet<>(field.getTextListValue()).contains(rule.getTextValue());
			}
			else if (org.opencdmp.commons.enums.FieldType.isReferenceType(fieldType)) {
				throw new NotImplementedException("Reference rule not supported");
			}
			else if (org.opencdmp.commons.enums.FieldType.isTagType(fieldType)) {
				throw new NotImplementedException("tags not supported");
			}
			else if (org.opencdmp.commons.enums.FieldType.isDateType(fieldType) && field.getDateValue() != null) return field.getDateValue().equals(rule.getDateValue());
			else if (org.opencdmp.commons.enums.FieldType.isBooleanType(fieldType) && field.getBooleanValue() != null) return field.getBooleanValue().equals(rule.getBooleanValue());
			else if (org.opencdmp.commons.enums.FieldType.isExternalIdentifierType(fieldType) && field.getExternalIdentifier() != null)  {
				throw new NotImplementedException("External identifier rule not supported");
			}
		}
		return false;
	}
}
