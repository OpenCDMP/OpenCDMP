import { Injectable, booleanAttribute } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { DescriptionTemplateDefinition, DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplatePage, DescriptionTemplateSection } from '@app/core/model/description-template/description-template';
import { RuleWithTarget } from './models/rule';
import { FormService } from '@common/forms/form-service';
import { DescriptionFieldPersist, DescriptionPropertyDefinitionFieldSetPersist, DescriptionPropertyDefinitionPersist } from '@app/core/model/description/description';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';

@Injectable()
export class VisibilityRulesService {
	private form: AbstractControl;
	private definition: DescriptionTemplateDefinition;
	private rulesBySources: Map<string, RuleWithTarget[]> ;
	private rulesByTarget: Map<string, RuleWithTarget[]> ;
	public isVisibleMap: { [key: string]: boolean } = {};
	private _isVisibleMap: { [key: string]: boolean } = null;

	private allDescriptionTemplateFields: DescriptionTemplateField[] = null;
	private allDescriptionTemplateFieldSets: DescriptionTemplateFieldSet[] = null;

	public rulesChangedSubject: Subject<{ [key: string]: boolean }>;

	constructor(
		protected formService: FormService
	) {
		this.rulesChangedSubject = new Subject<{ [key: string]: boolean }>();
	}

	public getRulesChangedObservable(): Observable<{ [key: string]: boolean }> {
		return this.rulesChangedSubject.asObservable();
	}

	public setContext(definition: DescriptionTemplateDefinition, form: AbstractControl) {
		this.definition = definition;
		this.form = form;
		this.allDescriptionTemplateFields = null;
		this.allDescriptionTemplateFieldSets = null;
		this.rulesBySources = null;
		this.rulesByTarget = null;
		this._isVisibleMap = null;
		this.calculateVisibility();
	}

	public buildVisibilityKey(id: string, ordinal: number | null): string {
		if (ordinal == null) return id;
		else return  id + "_" + ordinal;
	}

	public isVisible(id: string, ordinal: number | null): boolean {
		this.calculateVisibility();
		const fieldKey = this.buildVisibilityKey(id, ordinal);
		return this.isVisibleMap[fieldKey] ?? false;
	}

	public  getVisibilityStates(): { [key: string]: boolean } {
		this.calculateVisibility();
		return this.isVisibleMap;
	}

	public updateVisibilityForSource(id: string) {
		const visibilityRules = this.rulesBySources.has(id) ? this.rulesBySources.get(id) : null;
		if (visibilityRules && visibilityRules.length > 0) {
			this.reloadVisibility();
		}
	}

	public reloadVisibility() {
		this.rulesBySources = null;
		this.rulesByTarget = null;
		this._isVisibleMap = null;
		this.calculateVisibility();
	}

	private calculateVisibility(){
		if (this._isVisibleMap != null) return;
		if (this.definition == null || this.form == null) return;

		this.initRules();
		const propertyDefinition: DescriptionPropertyDefinitionPersist = this.formService.getValue(this.form.getRawValue()) as DescriptionPropertyDefinitionPersist;

		this.buildTargetVisibility(propertyDefinition);
		this.expandVisibilityToChildren(propertyDefinition);
		this.setDefaultVisibilityForNotCaclucted(propertyDefinition);
		this.hideParentIfAllChildrenAreHidden(propertyDefinition);
		this.ensureFieldSetVisibility(propertyDefinition);
		this.isVisibleMap = this._isVisibleMap;

		this.rulesChangedSubject.next(this._isVisibleMap);
	}

	private initRules(){
		if (this.definition == null || this.form == null) return;
		if (this.rulesBySources != null && this.rulesByTarget != null) return;
		this.rulesBySources = new Map();
		this.rulesByTarget = new Map();

		const fields: DescriptionTemplateField[] = this.getAllDescriptionTemplateDefinitionFields(this.definition);
		for (let i = 0; i < fields.length; i++) {
			const fieldEntity = fields[i];
			if (fieldEntity.visibilityRules != null && fieldEntity.visibilityRules.length > 0) {
				for (let j = 0; j < fieldEntity.visibilityRules.length; j++) {
					const rule = fieldEntity.visibilityRules[j];
					if (!this.rulesBySources.has(fieldEntity.id)) this.rulesBySources.set(fieldEntity.id, []);
					const ruleWithTarget: RuleWithTarget = new RuleWithTarget(fieldEntity.id, rule, fieldEntity);
					this.rulesBySources.get(fieldEntity.id).push(ruleWithTarget);

					if (!this.rulesByTarget.has(rule.target)) this.rulesByTarget.set(rule.target, []);
					this.rulesByTarget.get(rule.target).push(ruleWithTarget);
				}
			}
		}
	}

	private getDescriptionTemplateDefinitionFieldById(definition: DescriptionTemplateDefinition, fieldId: string): DescriptionTemplateField[] {
		const fields: DescriptionTemplateField[] = this.getAllDescriptionTemplateDefinitionFields(definition);
		return fields.filter(x=> x.id == fieldId);
	}

	private getAllDescriptionTemplateDefinitionFields(definition: DescriptionTemplateDefinition): DescriptionTemplateField[] {
		if (this.allDescriptionTemplateFields != null) return this.allDescriptionTemplateFields;

		let fields: DescriptionTemplateField[] = [];
		if (definition.pages == null) return fields;
		for (let i = 0; i < definition.pages.length; i++) {
			const item = definition.pages[i];
			fields = [...fields, ...this.getAllDescriptionTemplatePageFields(item)];
		}
		this.allDescriptionTemplateFields = fields;
		return this.allDescriptionTemplateFields;
	}

	private getAllDescriptionTemplatePageFields(definition: DescriptionTemplatePage): DescriptionTemplateField[] {
		let fields: DescriptionTemplateField[] = [];
		if (definition.sections == null) return fields;
		for (let i = 0; i < definition.sections.length; i++) {
			const item = definition.sections[i];
			fields = [...fields, ...this.getAllDescriptionTemplateSectionFields(item)];
		}
		return fields;
	}

	private getAllDescriptionTemplateSectionFields(definition: DescriptionTemplateSection): DescriptionTemplateField[] {
		let fields: DescriptionTemplateField[] = [];
		if (definition.sections != null) {
			for (let i = 0; i < definition.sections.length; i++) {
				const item = definition.sections[i];
				fields = [...fields, ...this.getAllDescriptionTemplateSectionFields(item)];
			}
		}
		if (definition.fieldSets != null) {
			for (let i = 0; i < definition.fieldSets.length; i++) {
				const item = definition.fieldSets[i];
				fields = [...fields, ...this.getAllDescriptionTemplateFieldSetFields(item)];
			}
		}
		return fields;
	}

	private getAllDescriptionTemplateFieldSetFields(definition: DescriptionTemplateFieldSet): DescriptionTemplateField[] {
		return definition.fields == null ? [] : definition.fields;
	}

	private getDescriptionTemplateDefinitionFieldSetById(definition: DescriptionTemplateDefinition, fieldSetId: string): DescriptionTemplateFieldSet[] {
		const fieldSets: DescriptionTemplateFieldSet[] = this.getAllDescriptionTemplateDefinitionFieldSets(definition);
		return fieldSets.filter(x=> x.id == fieldSetId);
	}

	private getAllDescriptionTemplateDefinitionFieldSets(definition: DescriptionTemplateDefinition): DescriptionTemplateFieldSet[] {
		if (this.allDescriptionTemplateFieldSets != null) return this.allDescriptionTemplateFieldSets;
		let fieldSets: DescriptionTemplateFieldSet[] = [];
		if (definition.pages == null) return fieldSets;
		for (let i = 0; i < definition.pages.length; i++) {
			const item = definition.pages[i];
			fieldSets = [...fieldSets, ...this.getAllDescriptionTemplatePageFieldSets(item)];
		}
		this.allDescriptionTemplateFieldSets = fieldSets;
		return this.allDescriptionTemplateFieldSets;
	}

	private getAllDescriptionTemplatePageFieldSets(definition: DescriptionTemplatePage): DescriptionTemplateFieldSet[] {
		let fieldSets: DescriptionTemplateFieldSet[] = [];
		if (definition.sections == null) return fieldSets;
		for (let i = 0; i < definition.sections.length; i++) {
			const item = definition.sections[i];
			fieldSets = [...fieldSets, ...this.getAllDescriptionTemplateSectionFieldSets(item)];
		}
		return fieldSets;
	}

	private getAllDescriptionTemplateSectionFieldSets(definition: DescriptionTemplateSection): DescriptionTemplateFieldSet[] {
		let fieldSets: DescriptionTemplateFieldSet[] = [];
		if (definition.sections != null) {
			for (let i = 0; i < definition.sections.length; i++) {
				const item = definition.sections[i];
				fieldSets = [...fieldSets, ...this.getAllDescriptionTemplateSectionFieldSets(item)];
			}
		}
		if (definition.fieldSets != null) {
			for (let i = 0; i < definition.fieldSets.length; i++) {
				const item = definition.fieldSets[i];
				fieldSets = [...fieldSets, ...definition.fieldSets];
			}
		}
		return fieldSets;
	}

	private buildTargetVisibility(propertyDefinition: DescriptionPropertyDefinitionPersist){
		this._isVisibleMap = {};
		this.rulesBySources.forEach((ruleForSource: RuleWithTarget[], ruleForSourceKey: string) => {
			for (let i = 0; i < ruleForSource.length; i++) {
				const rule = ruleForSource[i];
				if (propertyDefinition.fieldSets != null) {
					new Map(Object.entries(propertyDefinition.fieldSets)).forEach((propertyDefinitionFieldSet: DescriptionPropertyDefinitionFieldSetPersist, propertyDefinitionFieldSetKey: string) => {
						if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
							for (let j = 0; j < propertyDefinitionFieldSet.items.length; j++) {
								const definitionFieldSetItem = propertyDefinitionFieldSet.items[j];
								if (definitionFieldSetItem?.fields != null) {
									const fieldsMap = new Map(Object.entries(definitionFieldSetItem.fields));
									fieldsMap.forEach((field: DescriptionFieldPersist, key: string) => {
										if (rule.source == key){
											const rulesForParentKey: RuleWithTarget[] = this.getChainParentRules(rule);

											const parentIsVisible = rulesForParentKey != null && rulesForParentKey.length > 0 ? this.isChainParentVisible(rulesForParentKey, propertyDefinition, fieldsMap, definitionFieldSetItem.ordinal) : true;
											if (fieldsMap.has(rule.target)){ //Rule applies only for current multiple item
												const fieldKey = this.buildVisibilityKey(rule.target, definitionFieldSetItem.ordinal);
												const currentState = this._isVisibleMap[fieldKey] ?? false;
												this._isVisibleMap[fieldKey] = parentIsVisible && (currentState || this.ruleIsTrue(rule, field));
											} else if (this.getDescriptionTemplateDefinitionFieldById(this.definition, rule.target).length > 0 || this.getDescriptionTemplateDefinitionFieldSetById(this.definition, rule.target).length > 0) { //Rule applies to different fieldset, so we apply for all multiple items
												const ordinals: number[] = this.getKeyOrdinals(rule.target, propertyDefinition);
												for (let k = 0; k < ordinals.length; k++) {
													const ordinal = ordinals[k];
													const fieldKey = this.buildVisibilityKey(rule.target, ordinal);
													const currentState = this._isVisibleMap[fieldKey] ?? false;
													this._isVisibleMap[fieldKey] = parentIsVisible && (currentState || this.ruleIsTrue(rule, field));
												}
											} else {
												const fieldKey = this.buildVisibilityKey(rule.target, null); //Ordinal is null if target not on field
												const currentState = this._isVisibleMap[fieldKey] ?? false;
												this._isVisibleMap[fieldKey] = parentIsVisible && (currentState || this.ruleIsTrue(rule, field));
											}
										}
									});
								}
							}
						}
					});
				}
			}
		});
	}

	private isChainParentVisible(rulesForParentKey: RuleWithTarget[], propertyDefinition: DescriptionPropertyDefinitionPersist, fieldsMap: Map<string, DescriptionFieldPersist>, ordinal: number): boolean {
		let isVisible = false;
		if (rulesForParentKey == null || rulesForParentKey.length == 0) return false;

		for (let i = 0; i < rulesForParentKey.length; i++) {
			const ruleForParentKey = rulesForParentKey[i];

			if (ruleForParentKey.source == ruleForParentKey.target) continue; //Invalid rule where source and target are equal
			
			const field: DescriptionFieldPersist = fieldsMap.get(ruleForParentKey.source);

			const rulesForGrandParentKey: RuleWithTarget[] = this.getChainParentRules(ruleForParentKey);

			if (fieldsMap.has(ruleForParentKey.target)){ //Rule applies only for current multiple item
				const fieldKey = this.buildVisibilityKey(ruleForParentKey.target, ordinal);
				const currentState = this._isVisibleMap[fieldKey] ?? false;
				isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
				if (rulesForGrandParentKey != null && rulesForGrandParentKey.length > 0) isVisible = isVisible || this.isChainParentVisible(rulesForGrandParentKey, propertyDefinition, fieldsMap, ordinal);

			} else if (this.getDescriptionTemplateDefinitionFieldById(this.definition, ruleForParentKey.target).length > 0 || this.getDescriptionTemplateDefinitionFieldSetById(this.definition, ruleForParentKey.target).length > 0) { //Rule applies to different fieldset, so we apply for all multiple items
				const ordinals: number[] = this.getKeyOrdinals(ruleForParentKey.target, propertyDefinition);
				for (let k = 0; k < ordinals.length; k++) {
					const curentOrdinal = ordinals[k];
					const fieldKey = this.buildVisibilityKey(ruleForParentKey.target, curentOrdinal);
					const currentState = this._isVisibleMap[fieldKey] ?? false;
					isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
					if (rulesForGrandParentKey != null && rulesForGrandParentKey.length > 0) isVisible = isVisible || this.isChainParentVisible(rulesForGrandParentKey, propertyDefinition, this.getKeyFields(ruleForParentKey.target, curentOrdinal, propertyDefinition), ordinal);
				}
			} else {
				const fieldKey = this.buildVisibilityKey(ruleForParentKey.target, null); //Ordinal is null if target not on field
				const currentState = this._isVisibleMap[fieldKey] ?? false;
				isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
				//Nothing to check for grandfather this type of field can not have rules
			}

			if (isVisible) break;
		}
		return isVisible;
	}

	private getChainParentRules(rule: RuleWithTarget) : RuleWithTarget[] {
		if (!rule?.source || this.rulesByTarget == null) return null;
		return this.rulesByTarget?.get(rule.source)?.filter(x=> x != null);
	}

	private getKeyOrdinals(key: string, propertyDefinition: DescriptionPropertyDefinitionPersist): number[]{
		let ordinals = [];
		if (propertyDefinition.fieldSets != null) {
			new Map(Object.entries(propertyDefinition.fieldSets)).forEach((propertyDefinitionFieldSet: DescriptionPropertyDefinitionFieldSetPersist, propertyDefinitionFieldSetKey: string) => {
				if (propertyDefinitionFieldSetKey == key) {
					ordinals = propertyDefinitionFieldSet.items?.map(x => x.ordinal) ?? [];
					return ordinals;
				}
				if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
					for (let i = 0; i < propertyDefinitionFieldSet.items.length; i++) {
						const definitionFieldSetItem = propertyDefinitionFieldSet.items[i];
						if (definitionFieldSetItem?.fields != null) {
							new Map(Object.entries(definitionFieldSetItem.fields)).forEach((field: DescriptionFieldPersist, fieldKey: string) => {
								if (fieldKey == key) ordinals = propertyDefinitionFieldSet.items?.map(x=> x.ordinal) ?? [];
							});
							if (ordinals != null && ordinals.length > 0) return ordinals;
						}
					}
				}
			});
		}
		return ordinals;
	}

	private getKeyFields(key: string, ordinal: number, propertyDefinition: DescriptionPropertyDefinitionPersist): Map<string, DescriptionFieldPersist>{
		let fields: Map<string, DescriptionFieldPersist>;
		if (propertyDefinition.fieldSets != null) {
			new Map(Object.entries(propertyDefinition.fieldSets)).forEach((propertyDefinitionFieldSet: DescriptionPropertyDefinitionFieldSetPersist, propertyDefinitionFieldSetKey: string) => {
				if (propertyDefinitionFieldSetKey == key) {
					fields = propertyDefinitionFieldSet.items?.find(x => x.ordinal == ordinal)?.fields;
					return fields;
				}
				if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
					for (let i = 0; i < propertyDefinitionFieldSet.items.length; i++) {
						const definitionFieldSetItem = propertyDefinitionFieldSet.items[i];
						if (definitionFieldSetItem?.fields != null) {
							new Map(Object.entries(definitionFieldSetItem.fields)).forEach((field: DescriptionFieldPersist, fieldKey: string) => {
								if (fieldKey == key) fields = propertyDefinitionFieldSet.items?.find(x => x.ordinal == ordinal)?.fields;
							});
							if (fields != null && fields.size > 0) return fields;
						}
					}
				}
			});
		}
		return fields ? new Map<string, DescriptionFieldPersist>(Object.entries(fields)) : new Map<string, DescriptionFieldPersist>();
	}

	private ruleIsTrue(rule: RuleWithTarget, field: DescriptionFieldPersist) :boolean{
		if (field != null){
			const fieldType: DescriptionTemplateFieldType = rule.field != null && rule.field.data != null ? rule.field.data.fieldType :  DescriptionTemplateFieldType.FREE_TEXT;
			if ([DescriptionTemplateFieldType.FREE_TEXT, DescriptionTemplateFieldType.RADIO_BOX, DescriptionTemplateFieldType.TEXT_AREA,
				DescriptionTemplateFieldType.RICH_TEXT_AREA].includes(fieldType) && field.textValue != null && field.textValue.length > 0) {
				if (DescriptionTemplateFieldType.UPLOAD == fieldType){
					return false; //not apply visibility logic
				} else {
					return field.textValue == rule.textValue;
				}
			}
			else if ([DescriptionTemplateFieldType.SELECT].includes(fieldType) && field.textValue != null && field.textValue.length > 0) {
				return rule.textValue != null && rule.textValue.length > 0 && field.textValue == rule.textValue;
			}
			else if ([DescriptionTemplateFieldType.SELECT].includes(fieldType) && field.textListValue != null && field.textListValue.length > 0) {
				return field.textListValue.includes(rule.textValue);
			}
			else if ([DescriptionTemplateFieldType.REFERENCE_TYPES, DescriptionTemplateFieldType.UPLOAD, DescriptionTemplateFieldType.TAGS, DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS,
				DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS, DescriptionTemplateFieldType.VALIDATION, DescriptionTemplateFieldType.DATASET_IDENTIFIER].includes(fieldType)) {
				return false; //not implemented visibility logic
			}
			else if ([DescriptionTemplateFieldType.CHECK_BOX, DescriptionTemplateFieldType.BOOLEAN_DECISION].includes(fieldType)) {
				return field.booleanValue == rule.booleanValue;
			}
			else if (DescriptionTemplateFieldType.DATE_PICKER == fieldType && field.dateValue != null) return field.dateValue == rule.dateValue;
		}
		return false;
	}

	private expandVisibilityToChildren(propertyDefinition: DescriptionPropertyDefinitionPersist){
		if (this.definition?.pages == null) return;
		for (let i = 0; i < this.definition?.pages.length; i++) {
			const pageEntity = this.definition?.pages[i];
			const fieldKey = this.buildVisibilityKey(pageEntity.id, null);
			const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
			this.expandPageVisibility(pageEntity, propertyDefinition, currentValue);
		}
	}

	private expandPageVisibility(pageEntity : DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist, parentVisibility : boolean | null){
		if (pageEntity.sections == null) return;
		for (let i = 0; i < pageEntity.sections.length; i++) {
			const sectionEntity = pageEntity.sections[i];
			const fieldKey = this.buildVisibilityKey(sectionEntity.id, null);
			const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
			if (currentValue != null){
				if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
					this._isVisibleMap[fieldKey] = false;
					this.expandSectionVisibility(sectionEntity, propertyDefinition, currentValue);
				} else {
					this.expandSectionVisibility(sectionEntity, propertyDefinition, currentValue);
				}
			} else {
				if (parentVisibility != null) this._isVisibleMap[fieldKey] = parentVisibility;
				this.expandSectionVisibility(sectionEntity, propertyDefinition, parentVisibility);
			}
		}
	}
	private expandSectionVisibility(sectionEntity: DescriptionTemplateSection, propertyDefinition: DescriptionPropertyDefinitionPersist, parentVisibility : boolean | null){
		if (sectionEntity.sections != null) {
			for (let i = 0; i < sectionEntity.sections.length; i++) {
				const subSectionEntity = sectionEntity.sections[i];
				const fieldKey = this.buildVisibilityKey(subSectionEntity.id, null);
				const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
				if (currentValue != null){
					if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
						this._isVisibleMap[fieldKey] = false;
						this.expandSectionVisibility(subSectionEntity, propertyDefinition, currentValue);
					} else {
						this.expandSectionVisibility(subSectionEntity, propertyDefinition, currentValue);
					}
				} else {
					if (parentVisibility != null) this._isVisibleMap[fieldKey] = parentVisibility;
					this.expandSectionVisibility(subSectionEntity, propertyDefinition, parentVisibility);
				}
			}
		}
		if (sectionEntity.fieldSets != null) {

			for (let i = 0; i < sectionEntity.fieldSets.length; i++) {
				const fieldSetEntity = sectionEntity.fieldSets[i];
				const fieldSetsMap = propertyDefinition.fieldSets != null ? new Map(Object.entries(propertyDefinition.fieldSets)) : null;
				if (fieldSetsMap != null && fieldSetsMap.has(fieldSetEntity.id)) {
					const propertyDefinitionFieldSet = fieldSetsMap.get(fieldSetEntity.id)
					if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
						for (let j = 0; j < propertyDefinitionFieldSet.items.length; j++) {
							const definitionFieldSetItem = propertyDefinitionFieldSet.items[j];
							const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, definitionFieldSetItem.ordinal);
							const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
							if (currentValue != null){
								if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
									this._isVisibleMap[fieldKey] = false;
									this.expandFieldSetVisibility(fieldSetEntity, currentValue, definitionFieldSetItem.ordinal);
								} else {
									this.expandFieldSetVisibility(fieldSetEntity, currentValue, definitionFieldSetItem.ordinal);
								}
							} else {
								if (parentVisibility != null) this._isVisibleMap[fieldKey] = parentVisibility;
								this.expandFieldSetVisibility(fieldSetEntity, parentVisibility, definitionFieldSetItem.ordinal);
							}
						}
					}
				}
			}
		}
	}

	private expandFieldSetVisibility(fieldSetEntity: DescriptionTemplateFieldSet, parentVisibility: boolean | null, ordinal: number){
		if (fieldSetEntity.fields != null) {
			for (let i = 0; i < fieldSetEntity.fields.length; i++) {
				const fieldEntity = fieldSetEntity.fields[i];
				const fieldKey = this.buildVisibilityKey(fieldEntity.id, ordinal);
				const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
				if (currentValue != null){
					if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
						this._isVisibleMap[fieldKey] = false;
					}
				} else if (parentVisibility != null){
					this._isVisibleMap[fieldKey] = parentVisibility;
				}
			}
		}
	}

	private setDefaultVisibilityForNotCaclucted(propertyDefinition: DescriptionPropertyDefinitionPersist) {
		if (this.definition?.pages == null) return;
		for (let i = 0; i < this.definition?.pages.length; i++) {
			const pageEntity = this.definition?.pages[i];
			const fieldKey = this.buildVisibilityKey(pageEntity.id, null);
			const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
			if (currentValue == null) this._isVisibleMap[fieldKey] = true;
			this.setDefaultPageVisibility(pageEntity, propertyDefinition);
		}
	}

	private setDefaultPageVisibility(pageEntity: DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist) {
		if (pageEntity.sections == null) return;
		for (let i = 0; i < pageEntity.sections.length; i++) {
			const sectionEntity = pageEntity.sections[i];
			const fieldKey = this.buildVisibilityKey(sectionEntity.id, null);
			const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
			if (currentValue == null){
				this._isVisibleMap[fieldKey] = true;
				this.setDefaultSectionVisibility(sectionEntity, propertyDefinition);
			}
		}
	}

	private setDefaultSectionVisibility(sectionEntity: DescriptionTemplateSection, propertyDefinition: DescriptionPropertyDefinitionPersist) {
		if (sectionEntity.sections != null) {
			for (let i = 0; i < sectionEntity.sections.length; i++) {
				const subSectionEntity = sectionEntity.sections[i];
				const fieldKey = this.buildVisibilityKey(subSectionEntity.id, null);
				const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
				if (currentValue == null){
					this._isVisibleMap[fieldKey] = true;
					this.setDefaultSectionVisibility(subSectionEntity, propertyDefinition);
				}
			}
		}
		if (sectionEntity.fieldSets != null) {
			for (let i = 0; i < sectionEntity.fieldSets.length; i++) {
				const fieldSetEntity = sectionEntity.fieldSets[i];
				const fieldSetsMap = propertyDefinition.fieldSets != null ? new Map(Object.entries(propertyDefinition.fieldSets)) : null;
				if (fieldSetsMap != null && fieldSetsMap.has(fieldSetEntity.id)) {
					const propertyDefinitionFieldSet = fieldSetsMap.get(fieldSetEntity.id)
					if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
						for (let j = 0; j < propertyDefinitionFieldSet.items.length; j++) {
							const definitionFieldSetItem = propertyDefinitionFieldSet.items[j];
							const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, definitionFieldSetItem.ordinal);
							const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
							if (currentValue == null){
								this._isVisibleMap[fieldKey] = true;
								this.setDefaultFieldSetVisibility(fieldSetEntity, definitionFieldSetItem.ordinal);
							}
						}
					}
				}
			}
		}
	}

	private setDefaultFieldSetVisibility(fieldSetEntity: DescriptionTemplateFieldSet, ordinal: number){
		if (fieldSetEntity.fields != null) {
			for (let i = 0; i < fieldSetEntity.fields.length; i++) {
				const fieldEntity = fieldSetEntity.fields[i];
				const fieldKey = this.buildVisibilityKey(fieldEntity.id, ordinal);
				const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
				if (currentValue == null){
					this._isVisibleMap[fieldKey] = true;
				}
			}
		}
	}

	private hideParentIfAllChildrenAreHidden(propertyDefinition: DescriptionPropertyDefinitionPersist) {
		if (this.definition?.pages == null || this.definition?.pages.length  == 0) return;
		for (let i = 0; i < this.definition?.pages.length; i++) {
			const pageEntity = this.definition?.pages[i];
			const fieldKey = this.buildVisibilityKey(pageEntity.id, null);
			const isCurrentHidden = this.isHiddenPageVisibilityIfAllChildrenIsHidden(pageEntity, propertyDefinition);

			if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
				this._isVisibleMap[fieldKey] = false;
			}
		}
	}

	private isHiddenPageVisibilityIfAllChildrenIsHidden(pageEntity: DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist): boolean{
		let isHidden = true;
		if (pageEntity?.sections == null || pageEntity?.sections.length == 0) return false;
		for (let i = 0; i < pageEntity.sections.length; i++) {
			const sectionEntity = pageEntity.sections[i];
			const fieldKey = this.buildVisibilityKey(sectionEntity.id, null);
			const isCurrentHidden = this.isHiddenSectionIfAllChildrenIsHidden(sectionEntity, propertyDefinition);
			if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
				this._isVisibleMap[fieldKey] = false;
			}
			isHidden = isHidden && isCurrentHidden;
		}
		return isHidden;
	}

	private isHiddenSectionIfAllChildrenIsHidden(sectionEntity: DescriptionTemplateSection, propertyDefinition: DescriptionPropertyDefinitionPersist): boolean {
		let isHidden = true;
		if ((sectionEntity.sections == null || sectionEntity?.sections.length == 0) && (sectionEntity.fieldSets == null || sectionEntity?.fieldSets.length == 0)) return false;
		if (sectionEntity.sections != null) {
			for (let i = 0; i < sectionEntity.sections.length; i++) {
				const subSectionEntity = sectionEntity.sections[i];
				const fieldKey = this.buildVisibilityKey(subSectionEntity.id, null);
				const isCurrentHidden = this.isHiddenSectionIfAllChildrenIsHidden(subSectionEntity, propertyDefinition);
				if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
					this._isVisibleMap[fieldKey] = false;
				}
				isHidden = isHidden && isCurrentHidden;
			}
		}
		if (sectionEntity.fieldSets != null) {
			for (let i = 0; i < sectionEntity.fieldSets.length; i++) {
				const fieldSetEntity = sectionEntity.fieldSets[i];
				const fieldSetsMap = propertyDefinition.fieldSets != null ? new Map(Object.entries(propertyDefinition.fieldSets)) : null;
				if (fieldSetsMap != null && fieldSetsMap.has(fieldSetEntity.id)) {
					const propertyDefinitionFieldSet = fieldSetsMap.get(fieldSetEntity.id)
					if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
						for (let j = 0; j < propertyDefinitionFieldSet.items.length; j++) {
							const definitionFieldSetItem = propertyDefinitionFieldSet.items[j];
							const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, definitionFieldSetItem.ordinal);
							const isCurrentHidden = this.isHiddenFieldSetIfAllChildrenIsHidden(fieldSetEntity, definitionFieldSetItem.ordinal);
							if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
								this._isVisibleMap[fieldKey] = false;
							}
							isHidden = isHidden && isCurrentHidden;
						}
					}
				}
			}
		}
		return isHidden;
	}

	private isHiddenFieldSetIfAllChildrenIsHidden(fieldSetEntity: DescriptionTemplateFieldSet, ordinal: number): boolean{
		let isHidden = true;
		if (fieldSetEntity?.fields == null || fieldSetEntity?.fields.length == 0) return false;
		for (let i = 0; i < fieldSetEntity.fields.length; i++) {
			const fieldEntity = fieldSetEntity.fields[i];
			const fieldKey = this.buildVisibilityKey(fieldEntity.id, ordinal);
			const currentValue: boolean | null = (this._isVisibleMap[fieldKey] ?? true);
			isHidden = isHidden && !currentValue;
		}
		return isHidden;
	}

	private ensureFieldSetVisibility(propertyDefinition: DescriptionPropertyDefinitionPersist) {
		if (this.definition?.pages == null) return;
		for (let i = 0; i < this.definition?.pages.length; i++) {
			const pageEntity = this.definition?.pages[i];
			this.ensurePageFieldSetVisibility(pageEntity, propertyDefinition);
		}
	}

	private ensurePageFieldSetVisibility(pageEntity: DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist){
		if (pageEntity?.sections == null) return;
		for (let i = 0; i < pageEntity.sections.length; i++) {
			const sectionEntity = pageEntity.sections[i];
			this.ensureSectionFieldSetVisibility(sectionEntity, propertyDefinition);
		}
	}

	private ensureSectionFieldSetVisibility(sectionEntity: DescriptionTemplateSection, propertyDefinition: DescriptionPropertyDefinitionPersist){
		if (sectionEntity.sections != null) {
			for (let i = 0; i < sectionEntity.sections.length; i++) {
				const subSectionEntity = sectionEntity.sections[i];
				this.ensureSectionFieldSetVisibility(subSectionEntity, propertyDefinition);
			}
		}
		if (sectionEntity.fieldSets != null) {
			for (let i = 0; i < sectionEntity.fieldSets.length; i++) {
				const fieldSetEntity = sectionEntity.fieldSets[i];
				let isHidden = true;
				const fieldSetsMap = propertyDefinition.fieldSets != null ? new Map(Object.entries(propertyDefinition.fieldSets)) : null;
				if (fieldSetsMap != null && fieldSetsMap.has(fieldSetEntity.id)) {
					const propertyDefinitionFieldSet = fieldSetsMap.get(fieldSetEntity.id)
					if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
						for (let j = 0; j < propertyDefinitionFieldSet.items.length; j++) {
							const definitionFieldSetItem = propertyDefinitionFieldSet.items[j];
							const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, definitionFieldSetItem.ordinal);
							const isCurrentHidden = !this._isVisibleMap[fieldKey] ?? false;
							isHidden = isHidden && isCurrentHidden;
						}
					}
				}
				const globalFieldSetKey = this.buildVisibilityKey(fieldSetEntity.id, null);
				this._isVisibleMap[globalFieldSetKey] = !isHidden;
			}
		}
	}
}
