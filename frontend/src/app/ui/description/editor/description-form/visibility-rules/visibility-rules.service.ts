import { Injectable } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { DescriptionTemplateDefinition, BaseField, BaseFieldSet, BaseSection } from '@app/core/model/description-template/description-template';
import { RuleWithTarget } from './models/rule';
import { FormService } from '@common/forms/form-service';
import { FieldPersist, PropertyDefinition, PropertyDefinitionFieldSetPersist, PropertyDefinitionPersist } from '@app/core/model/description/description';
import { FieldType } from '@app/core/common/enum/field-type';
import { PlanBlueprintDefinition, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';

@Injectable()
export class VisibilityRulesService {
    private propertyDefinition: PropertyDefinition;
    private propertyDefinitionForm: AbstractControl;
	private definition: DescriptionTemplateDefinition | PlanBlueprintDefinition;
	private rulesBySources: Map<string, RuleWithTarget[]> ;
	private rulesByTarget: Map<string, RuleWithTarget[]> ;
	public isVisibleMap: { [key: string]: boolean } = {};
	private _isVisibleMap: { [key: string]: boolean } = null;

	private allFields: BaseField[] = null;
	private allFieldSets: BaseFieldSet[] = null;

	public rulesChangedSubject: Subject<{ [key: string]: boolean }>;

	constructor(
		protected formService: FormService
	) {
		this.rulesChangedSubject = new Subject<{ [key: string]: boolean }>();
	}

	public getRulesChangedObservable(): Observable<{ [key: string]: boolean }> {
		return this.rulesChangedSubject.asObservable();
	}

	public setContext(
        definition: DescriptionTemplateDefinition | PlanBlueprintDefinition,
        propertyDefinitionForm?: AbstractControl,
        propertyDefinition?: PropertyDefinition
    ) {
		this.definition = definition;
        this.propertyDefinitionForm = propertyDefinitionForm;
		this.propertyDefinition =  propertyDefinition;
		this.allFields = null;
		this.allFieldSets = null;
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
		// FE-V (clear-on-save policy): we intentionally do NOT clear hidden field values here on edit.
		// Values are preserved in the form while editing — toggling a controlling field off and back on
		// restores the prior answer — and hidden values are stripped from the payload at save time
		// instead (see stripHiddenFieldValues, called from the plan/description persist paths). Chain
		// gating (isChainParentVisible) means a retained value on a now-hidden source can't mis-fire a
		// downstream rule, so keeping values during editing is safe.
		//
		// FE-V: visibility just changed, so the required-ness enforced by
		// RequiredWithVisibilityRulesValidator changed too. Re-run validators on the affected controls
		// so a field that became hidden drops its now-stale `required` error and one that became
		// visible regains it. Without this, isVisibleMap is updated but control.errors is not, leaving
		// stale validation state (e.g. a hidden field still blocking finalize).
		this.revalidateVisibilityDependentControls();
	}

	/** Value-bearing control names a field form group may carry (see DescriptionEditorModel.getFieldValueControlName). */
	private static readonly VALUE_PROP_NAMES = [
		'textValue', 'textListValue', 'dateValue', 'booleanValue', 'numberValue',
		'references', 'reference', 'tags', 'externalIdentifier', 'contacts',
	];

	/**
	 * FE-V (clear-on-save): null out the value of every currently-hidden field in a raw property
	 * definition object (e.g. `plan/description getRawValue().properties`). Mutates the passed object,
	 * which is a getRawValue() copy — the live form is untouched, so answers stay intact while editing
	 * and are only removed from what gets persisted. Call this on the persist payload immediately
	 * before sending.
	 */
	public stripHiddenFieldValues(propertyDefinition: { fieldSets?: { [id: string]: { items?: any[] } } } | null | undefined): void {
		if (!propertyDefinition?.fieldSets) return;
		this.calculateVisibility();
		if (!this._isVisibleMap) return;
		Object.keys(propertyDefinition.fieldSets).forEach((fieldSetId) => {
			const items = propertyDefinition.fieldSets[fieldSetId]?.items ?? [];
			for (const item of items) {
				const ordinal = item?.ordinal;
				const fields = item?.fields;
				if (!fields) continue;
				Object.keys(fields).forEach((fieldId) => {
					const key = this.buildVisibilityKey(fieldId, ordinal);
					if (this._isVisibleMap[key] === false) {
						this.clearFieldValueObject(fields[fieldId]);
					}
				});
			}
		});
	}

	private clearFieldValueObject(fieldValue: { [k: string]: any } | null | undefined): void {
		if (!fieldValue) return;
		VisibilityRulesService.VALUE_PROP_NAMES.forEach((prop) => {
			if (prop in fieldValue) {
				fieldValue[prop] = Array.isArray(fieldValue[prop]) ? [] : null;
			}
		});
	}

	/**
	 * FE-V: re-run validators on every field control after a visibility recompute. Mirrors the walk in
	 * clearHiddenFieldValues. `emitEvent:false` avoids re-triggering the visibility recompute loop
	 * (form-field.component subscribes to valueChanges). updateValueAndValidity bubbles up by default,
	 * so ancestor groups / the multiplicity items array (ValidateIfVisible minLength) re-validate too.
	 */
	private revalidateVisibilityDependentControls() {
		const fieldSetsGroup = this.propertyDefinitionForm?.get('fieldSets');
		const fieldSetControls = (fieldSetsGroup as any)?.controls;
		if (!fieldSetControls) return;
		Object.keys(fieldSetControls).forEach((fieldSetId) => {
			const itemControls = (fieldSetControls[fieldSetId]?.get('items') as any)?.controls ?? [];
			for (const itemControl of itemControls) {
				const fieldControls = (itemControl?.get('fields') as any)?.controls;
				if (!fieldControls) continue;
				Object.keys(fieldControls).forEach((fieldId) => {
					const control: AbstractControl = fieldControls[fieldId];
					control?.updateValueAndValidity({ emitEvent: false });
				});
			}
		});
	}

	private calculateVisibility(){
		if (this._isVisibleMap != null) return;
		if (this.definition == null || (this.propertyDefinitionForm == null && this.propertyDefinition == null)) return;
        const propertyDefinition = this.propertyDefinitionForm ? this.propertyDefinitionForm.getRawValue() : this.propertyDefinition;
		this.initRules();
		this.buildTargetVisibility(propertyDefinition);
		this.expandVisibilityToChildren(propertyDefinition);
		this.setDefaultVisibilityForNotCaclucted(propertyDefinition);
		this.hideParentIfAllChildrenAreHidden(propertyDefinition);
		this.ensureFieldSetVisibility(propertyDefinition);
		this.isVisibleMap = this._isVisibleMap;

		this.rulesChangedSubject.next(this._isVisibleMap);
	}

	private initRules(){
		if (this.definition == null || (this.propertyDefinitionForm == null && this.propertyDefinition == null)) return;
		if (this.rulesBySources != null && this.rulesByTarget != null) return;
		this.rulesBySources = new Map();
		this.rulesByTarget = new Map();

		const fields: BaseField[] = this.getAllDefinitionFields(this.definition);
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

	private getDefinitionFieldById(definition: DescriptionTemplateDefinition | PlanBlueprintDefinition, fieldId: string): BaseField[] {
		const fields: BaseField[] = this.getAllDefinitionFields(definition);
		return fields.filter(x=> x.id == fieldId);
	}

	private getAllDefinitionFields(definition: DescriptionTemplateDefinition | PlanBlueprintDefinition): BaseField[] {
		if (this.allFields != null) return this.allFields;

		let fields: BaseField[] = [];
		if (definition.sections == null) return fields;
		for (let i = 0; i < definition.sections.length; i++) {
			const item = definition.sections[i];
			fields = [...fields, ...this.getAllSectionFields(item)];
		}
		this.allFields = fields;
		return this.allFields;
	}

	// private getAllDescriptionTemplatePageFields(definition: DescriptionTemplatePage): DescriptionTemplateField[] {
	// 	let fields: DescriptionTemplateField[] = [];
	// 	if (definition.sections == null) return fields;
	// 	for (let i = 0; i < definition.sections.length; i++) {
	// 		const item = definition.sections[i];
	// 		fields = [...fields, ...this.getAllDescriptionTemplateSectionFields(item)];
	// 	}
	// 	return fields;
	// }

	private getAllSectionFields(definition: BaseSection | RootSection): BaseField[] {
		let fields: BaseField[] = [];
		if (definition.sections != null) {
			for (let i = 0; i < definition.sections.length; i++) {
				const item = definition.sections[i];
				fields = [...fields, ...this.getAllSectionFields(item)];
			}
		}
		if (definition.fieldSets != null) {
			for (let i = 0; i < definition.fieldSets.length; i++) {
				const item = definition.fieldSets[i];
				fields = [...fields, ...this.getAllFieldSetFields(item)];
			}
		}
		return fields;
	}

	private getAllFieldSetFields(definition: BaseFieldSet): BaseField[] {
		return definition.fields == null ? [] : definition.fields;
	}

	private getDefinitionFieldSetById(definition: DescriptionTemplateDefinition | PlanBlueprintDefinition, fieldSetId: string): BaseFieldSet[] {
		const fieldSets: BaseFieldSet[] = this.getAllDefinitionFieldSets(definition);
		return fieldSets.filter(x=> x.id == fieldSetId);
	}

	private getAllDefinitionFieldSets(definition: DescriptionTemplateDefinition | PlanBlueprintDefinition): BaseFieldSet[] {
		if (this.allFieldSets != null) return this.allFieldSets;
		let fieldSets: BaseFieldSet[] = [];
		if (definition.sections == null) return fieldSets;
		for (let i = 0; i < definition.sections.length; i++) {
			const item = definition.sections[i];
			fieldSets = [...fieldSets, ...this.getAllSectionFieldSets(item)];
		}
		this.allFieldSets = fieldSets;
		return this.allFieldSets;
	}

	// private getAllDescriptionTemplatePageFieldSets(definition: DescriptionTemplatePage): DescriptionTemplateFieldSet[] {
	// 	let fieldSets: DescriptionTemplateFieldSet[] = [];
	// 	if (definition.sections == null) return fieldSets;
	// 	for (let i = 0; i < definition.sections.length; i++) {
	// 		const item = definition.sections[i];
	// 		fieldSets = [...fieldSets, ...this.getAllDescriptionTemplateSectionFieldSets(item)];
	// 	}
	// 	return fieldSets;
	// }

	private getAllSectionFieldSets(definition: BaseSection | RootSection): BaseFieldSet[] {
		let fieldSets: BaseFieldSet[] = [];
		if (definition.sections != null) {
			for (let i = 0; i < definition.sections.length; i++) {
				const item = definition.sections[i];
				fieldSets = [...fieldSets, ...this.getAllSectionFieldSets(item)];
			}
		}
		if (definition.fieldSets != null) {
			fieldSets = [...fieldSets, ...definition.fieldSets];
		}
		return fieldSets;
	}

	private buildTargetVisibility(propertyDefinition: PropertyDefinitionPersist | PropertyDefinition){
		this._isVisibleMap = {};
		this.rulesBySources.forEach((ruleForSource: RuleWithTarget[], ruleForSourceKey: string) => {
			for (let i = 0; i < ruleForSource.length; i++) {
				const rule = ruleForSource[i];
				if (propertyDefinition.fieldSets != null) {
					new Map(Object.entries(propertyDefinition.fieldSets)).forEach((propertyDefinitionFieldSet: PropertyDefinitionFieldSetPersist, propertyDefinitionFieldSetKey: string) => {
						if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
							for (let j = 0; j < propertyDefinitionFieldSet.items.length; j++) {
								const definitionFieldSetItem = propertyDefinitionFieldSet.items[j];
								if (definitionFieldSetItem?.fields != null) {
									const fieldsMap = new Map(Object.entries(definitionFieldSetItem.fields));
									fieldsMap.forEach((field: FieldPersist, key: string) => {
										if (rule.source == key){
											const rulesForParentKey: RuleWithTarget[] = this.getChainParentRules(rule);

											const parentIsVisible = rulesForParentKey != null && rulesForParentKey.length > 0 ? this.isChainParentVisible(rulesForParentKey, propertyDefinition, fieldsMap, definitionFieldSetItem.ordinal) : true;
											if (fieldsMap.has(rule.target)){ //Rule applies only for current multiple item
												const fieldKey = this.buildVisibilityKey(rule.target, definitionFieldSetItem.ordinal);
												const currentState = this._isVisibleMap[fieldKey] ?? false;
												this._isVisibleMap[fieldKey] = parentIsVisible && (currentState || this.ruleIsTrue(rule, field));
											} else if (this.getDefinitionFieldById(this.definition, rule.target).length > 0 || this.getDefinitionFieldSetById(this.definition, rule.target).length > 0) { //Rule applies to different fieldset, so we apply for all multiple items
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

	private isChainParentVisible(rulesForParentKey: RuleWithTarget[], propertyDefinition: PropertyDefinitionPersist | PropertyDefinition, fieldsMap: Map<string, FieldPersist>, ordinal: number): boolean {
		let isVisible = false;
		if (rulesForParentKey == null || rulesForParentKey.length == 0) return false;

		for (let i = 0; i < rulesForParentKey.length; i++) {
			const ruleForParentKey = rulesForParentKey[i];

			if (ruleForParentKey.source == ruleForParentKey.target) continue; //Invalid rule where source and target are equal
			
			const field: FieldPersist = fieldsMap.get(ruleForParentKey.source);

			const rulesForGrandParentKey: RuleWithTarget[] = this.getChainParentRules(ruleForParentKey);

			if (fieldsMap.has(ruleForParentKey.target)){ //Rule applies only for current multiple item
				const fieldKey = this.buildVisibilityKey(ruleForParentKey.target, ordinal);
				const currentState = this._isVisibleMap[fieldKey] ?? false;
				isVisible = isVisible || currentState || this.ruleIsTrue(ruleForParentKey, field);
				if (rulesForGrandParentKey != null && rulesForGrandParentKey.length > 0) isVisible = isVisible || this.isChainParentVisible(rulesForGrandParentKey, propertyDefinition, fieldsMap, ordinal);

			} else if (this.getDefinitionFieldById(this.definition, ruleForParentKey.target).length > 0 || this.getDefinitionFieldSetById(this.definition, ruleForParentKey.target).length > 0) { //Rule applies to different fieldset, so we apply for all multiple items
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

	private getKeyOrdinals(key: string, propertyDefinition: PropertyDefinitionPersist | PropertyDefinition): number[]{
		let ordinals = [];
		if (propertyDefinition.fieldSets != null) {
			new Map(Object.entries(propertyDefinition.fieldSets)).forEach((propertyDefinitionFieldSet: PropertyDefinitionFieldSetPersist, propertyDefinitionFieldSetKey: string) => {
				if (propertyDefinitionFieldSetKey == key) {
					ordinals = propertyDefinitionFieldSet.items?.map(x => x.ordinal) ?? [];
					return ordinals;
				}
				if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
					for (let i = 0; i < propertyDefinitionFieldSet.items.length; i++) {
						const definitionFieldSetItem = propertyDefinitionFieldSet.items[i];
						if (definitionFieldSetItem?.fields != null) {
							new Map(Object.entries(definitionFieldSetItem.fields)).forEach((field: FieldPersist, fieldKey: string) => {
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

	private getKeyFields(key: string, ordinal: number, propertyDefinition: PropertyDefinitionPersist | PropertyDefinition): Map<string, FieldPersist>{
		let fields: Map<string, FieldPersist>;
		if (propertyDefinition.fieldSets != null) {
			new Map(Object.entries(propertyDefinition.fieldSets)).forEach((propertyDefinitionFieldSet: PropertyDefinitionFieldSetPersist, propertyDefinitionFieldSetKey: string) => {
				if (propertyDefinitionFieldSetKey == key) {
					fields = propertyDefinitionFieldSet.items?.find(x => x.ordinal == ordinal)?.fields;
					return fields;
				}
				if (propertyDefinitionFieldSet.items != null && propertyDefinitionFieldSet.items.length > 0) {
					for (let i = 0; i < propertyDefinitionFieldSet.items.length; i++) {
						const definitionFieldSetItem = propertyDefinitionFieldSet.items[i];
						if (definitionFieldSetItem?.fields != null) {
							new Map(Object.entries(definitionFieldSetItem.fields)).forEach((field: FieldPersist, fieldKey: string) => {
								if (fieldKey == key) fields = propertyDefinitionFieldSet.items?.find(x => x.ordinal == ordinal)?.fields;
							});
							if (fields != null && fields.size > 0) return fields;
						}
					}
				}
			});
		}
		return fields ? new Map<string, FieldPersist>(Object.entries(fields)) : new Map<string, FieldPersist>();
	}

	private ruleIsTrue(rule: RuleWithTarget, field: FieldPersist) :boolean{
		if (field != null){
			const fieldType: FieldType = rule.field != null && rule.field.data != null ? rule.field.data.fieldType :  FieldType.FREE_TEXT;
			if ([FieldType.FREE_TEXT, FieldType.RADIO_BOX, FieldType.TEXT_AREA,
				FieldType.RICH_TEXT_AREA].includes(fieldType) && field.textValue != null && field.textValue.length > 0) {
				if (FieldType.UPLOAD == fieldType){
					return false; //not apply visibility logic
				} else {
					return field.textValue == rule.textValue;
				}
			}
			else if ([FieldType.SELECT].includes(fieldType) && field.textValue != null && field.textValue.length > 0) {
				return rule.textValue != null && rule.textValue.length > 0 && field.textValue == rule.textValue;
			}
			else if ([FieldType.SELECT].includes(fieldType) && field.textListValue != null && field.textListValue.length > 0) {
				return field.textListValue.includes(rule.textValue);
			}
			else if ([FieldType.REFERENCE_TYPES, FieldType.UPLOAD, FieldType.TAGS, FieldType.INTERNAL_ENTRIES_PLANS,
				FieldType.INTERNAL_ENTRIES_DESCRIPTIONS, FieldType.VALIDATION, FieldType.DATASET_IDENTIFIER].includes(fieldType)) {
				return false; //not implemented visibility logic
			}
			else if ([FieldType.CHECK_BOX, FieldType.BOOLEAN_DECISION].includes(fieldType)) {
				return field.booleanValue == rule.booleanValue;
			}
			else if (FieldType.DATE_PICKER == fieldType && field.dateValue != null) return field.dateValue == rule.dateValue;
            else if(FieldType.NUMBER == fieldType && field.numberValue != null) return field.numberValue == rule.numberValue;
		}
		return false;
	}

	private expandVisibilityToChildren(propertyDefinition: PropertyDefinitionPersist | PropertyDefinition){
		if (this.definition?.sections == null) return;
		for (let i = 0; i < this.definition?.sections.length; i++) {
			const pageEntity = this.definition?.sections[i];
			const fieldKey = this.buildVisibilityKey(pageEntity.id, null);
			const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
			this.expandSectionVisibility(pageEntity, propertyDefinition, currentValue);
		}
	}

	// private expandPageVisibility(pageEntity : DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist | DescriptionPropertyDefinition, parentVisibility : boolean | null){
	// 	if (pageEntity.sections == null) return;
	// 	for (let i = 0; i < pageEntity.sections.length; i++) {
	// 		const sectionEntity = pageEntity.sections[i];
	// 		const fieldKey = this.buildVisibilityKey(sectionEntity.id, null);
	// 		const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
	// 		if (currentValue != null){
	// 			if (parentVisibility != null && !parentVisibility) { //Parent is hidden so all childs should be hidden
	// 				this._isVisibleMap[fieldKey] = false;
	// 				this.expandSectionVisibility(sectionEntity, propertyDefinition, currentValue);
	// 			} else {
	// 				this.expandSectionVisibility(sectionEntity, propertyDefinition, currentValue);
	// 			}
	// 		} else {
	// 			if (parentVisibility != null) this._isVisibleMap[fieldKey] = parentVisibility;
	// 			this.expandSectionVisibility(sectionEntity, propertyDefinition, parentVisibility);
	// 		}
	// 	}
	// }
	private expandSectionVisibility(sectionEntity: BaseSection, propertyDefinition: PropertyDefinitionPersist | PropertyDefinition, parentVisibility : boolean | null){
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

	private expandFieldSetVisibility(fieldSetEntity: BaseFieldSet, parentVisibility: boolean | null, ordinal: number){
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

	private setDefaultVisibilityForNotCaclucted(propertyDefinition: PropertyDefinitionPersist | PropertyDefinition) {
		if (this.definition?.sections == null) return;
		for (let i = 0; i < this.definition?.sections.length; i++) {
			const pageEntity = this.definition?.sections[i];
			const fieldKey = this.buildVisibilityKey(pageEntity.id, null);
			const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
			if (currentValue == null) this._isVisibleMap[fieldKey] = true;
			this.setDefaultSectionVisibility(pageEntity, propertyDefinition);
		}
	}

	// private setDefaultPageVisibility(pageEntity: DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist | DescriptionPropertyDefinition) {
	// 	if (pageEntity.sections == null) return;
	// 	for (let i = 0; i < pageEntity.sections.length; i++) {
	// 		const sectionEntity = pageEntity.sections[i];
	// 		const fieldKey = this.buildVisibilityKey(sectionEntity.id, null);
	// 		const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
	// 		if (currentValue == null){
	// 			this._isVisibleMap[fieldKey] = true;
	// 			this.setDefaultSectionVisibility(sectionEntity, propertyDefinition);
	// 		}
	// 	}
	// }

	private setDefaultSectionVisibility(sectionEntity: BaseSection, propertyDefinition: PropertyDefinitionPersist | PropertyDefinition) {
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
				}else {
                    const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, 0);
                    const currentValue: boolean | null = this._isVisibleMap[fieldKey] ?? null;
                    if (currentValue == null){
                        this._isVisibleMap[fieldKey] = true;
                        this.setDefaultFieldSetVisibility(fieldSetEntity, 0);
                    }
                }
			}
		}
	}

	private setDefaultFieldSetVisibility(fieldSetEntity: BaseFieldSet, ordinal: number){
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

	private hideParentIfAllChildrenAreHidden(propertyDefinition: PropertyDefinitionPersist | PropertyDefinition) {
		if (this.definition?.sections == null || this.definition?.sections.length  == 0) return;
		for (let i = 0; i < this.definition?.sections.length; i++) {
			const pageEntity = this.definition?.sections[i];
			const fieldKey = this.buildVisibilityKey(pageEntity.id, null);
			const isCurrentHidden = this.isHiddenSectionIfAllChildrenIsHidden(pageEntity, propertyDefinition);

			if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
				this._isVisibleMap[fieldKey] = false;
			}
		}
	}

	// private isHiddenPageVisibilityIfAllChildrenIsHidden(pageEntity: DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist | DescriptionPropertyDefinition): boolean{
	// 	let isHidden = true;
	// 	if (pageEntity?.sections == null || pageEntity?.sections.length == 0) return false;
	// 	for (let i = 0; i < pageEntity.sections.length; i++) {
	// 		const sectionEntity = pageEntity.sections[i];
	// 		const fieldKey = this.buildVisibilityKey(sectionEntity.id, null);
	// 		const isCurrentHidden = this.isHiddenSectionIfAllChildrenIsHidden(sectionEntity, propertyDefinition);
	// 		if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
	// 			this._isVisibleMap[fieldKey] = false;
	// 		}
	// 		isHidden = isHidden && isCurrentHidden;
	// 	}
	// 	return isHidden;
	// }

	private isHiddenSectionIfAllChildrenIsHidden(sectionEntity: BaseSection, propertyDefinition: PropertyDefinitionPersist | PropertyDefinition): boolean {
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
				}else {
                    const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, 0);
                    const isCurrentHidden = this.isHiddenFieldSetIfAllChildrenIsHidden(fieldSetEntity, 0);
                    if (isCurrentHidden && (this._isVisibleMap[fieldKey] ?? true)) {
                        this._isVisibleMap[fieldKey] = false;
                    }
                    isHidden = isHidden && isCurrentHidden;
                }
			}
		}
		return isHidden;
	}

	private isHiddenFieldSetIfAllChildrenIsHidden(fieldSetEntity: BaseFieldSet, ordinal: number): boolean{
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

	private ensureFieldSetVisibility(propertyDefinition: PropertyDefinitionPersist | PropertyDefinition) {
		if (this.definition?.sections == null) return;
		for (let i = 0; i < this.definition?.sections.length; i++) {
			const pageEntity = this.definition?.sections[i];
			this.ensureSectionFieldSetVisibility(pageEntity, propertyDefinition);
		}
	}

	// private ensurePageFieldSetVisibility(pageEntity: DescriptionTemplatePage, propertyDefinition: DescriptionPropertyDefinitionPersist | DescriptionPropertyDefinition){
	// 	if (pageEntity?.sections == null) return;
	// 	for (let i = 0; i < pageEntity.sections.length; i++) {
	// 		const sectionEntity = pageEntity.sections[i];
	// 		this.ensureSectionFieldSetVisibility(sectionEntity, propertyDefinition);
	// 	}
	// }

	private ensureSectionFieldSetVisibility(sectionEntity: BaseSection, propertyDefinition: PropertyDefinitionPersist | PropertyDefinition){
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
							const isCurrentHidden = !this._isVisibleMap[fieldKey];
							isHidden = isHidden && isCurrentHidden;
						}
					}
				}else {
                    const fieldKey = this.buildVisibilityKey(fieldSetEntity.id, 0);
                    const isCurrentHidden = !this._isVisibleMap[fieldKey];
                    isHidden = isHidden && isCurrentHidden;
                }
				const globalFieldSetKey = this.buildVisibilityKey(fieldSetEntity.id, null);
				this._isVisibleMap[globalFieldSetKey] = !isHidden;
			}
		}
	}

    ngOnDelete(){
        this.rulesChangedSubject.complete();
        this.rulesChangedSubject.unsubscribe();
    }
}
