import { BaseFieldSet, BaseSection, DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { ToCEntry } from "../models/toc-entry";
import { ToCEntryType } from "../models/toc-entry-type.enum";
import { AbstractControl, FormGroup } from "@angular/forms";
import { DescriptionEditorForm, FieldSetsFormGroup } from "../../description-editor.model";
import { PlanEditorForm } from "@app/ui/plan/plan-editor-blueprint/plan-editor.model";
import { RootToCEntry } from "@app/ui/plan/plan-editor-blueprint/plan-table-of-contents/plan-table-of-contents.component";
import { FieldType } from "@app/core/common/enum/field-type";
import { isFieldSetRequired } from "../../description-form/field-required.helper";
import { TemplateFieldEditorModel } from "@app/ui/admin/description-template/editor/description-template-editor.model";

export namespace TableOfContentsHelper {
    export function findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
        if (!tocentries || !tocentries.length) {
            return null;
        }

        let tocEntryFound = tocentries.find(entry => entry.id === id);

        if (tocEntryFound) {
            return tocEntryFound;
        }

        for (const entry of tocentries) {
            if(entry.type == ToCEntryType.RootPlanSection){
                const result = TableOfContentsHelper.findTocEntryById(id, (entry as RootToCEntry).descriptions);
                if(result){
                   tocEntryFound = result;
                    break; 
                }
            }
            const result = TableOfContentsHelper.findTocEntryById(id, entry.subEntries);
            if (result) {
                tocEntryFound = result;
                break;
            }
        }

        return tocEntryFound ? tocEntryFound : null;
    }

    export function getHeadField(item: ToCEntry): ToCEntry {
        if (!item) return null;

        const subEntries = item.subEntries;
        
        if (subEntries && subEntries.length) return TableOfContentsHelper.getHeadField(subEntries[0]);
        else if((item as RootToCEntry)?.descriptions?.length) return TableOfContentsHelper.getHeadField((item as RootToCEntry).descriptions[0])
        else return item;
    }
    
    export function getTailField(item: ToCEntry): ToCEntry {
        if (!item) return null;

        if((item as RootToCEntry).descriptions?.length) return TableOfContentsHelper.getTailField((item as RootToCEntry).descriptions[(item as RootToCEntry).descriptions.length - 1]);
        if (item.subEntries?.length) return TableOfContentsHelper.getTailField(item.subEntries[item.subEntries.length-1]);
        else return item;
    }

    export function setTocEntryNext(previous: ToCEntry, next: ToCEntry): ToCEntry {
		if (!previous) return null;

		if (previous.subEntries && previous.subEntries.length) {
			const last: number = previous.subEntries.length-1;
			previous.subEntries[last] = TableOfContentsHelper.setTocEntryNext(previous.subEntries[last], next);
			return previous;
		}
		else {
			previous.nextEntry = next;
			previous.isLastEntry = next == null
			return previous;
		}
	}
        
    export function buildRecursivelySection(params: {
        item: BaseSection,
        previousTocEntry?: ToCEntry,
        path: string[],
        fieldSetFormGroup: FormGroup<{[key: string]: FieldSetsFormGroup}>,
        formGroup: FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>
    }): ToCEntry {
        let {item, previousTocEntry, path, fieldSetFormGroup, formGroup} = params;
        if (!item) return null;

        const sections = item.sections;
        const fieldsets = item.fieldSets;

        const tempResult: ToCEntry[] = [];


        if (sections && sections.length) {
            sections.forEach(section => {
                if (previousTocEntry == null) {
                    const tocentry = TableOfContentsHelper.buildRecursivelySection({
                        item: section,
                        path: [...path, item.id],
                        fieldSetFormGroup,
                        formGroup
                    });
                    if (tocentry != null) {
                        tempResult.push(tocentry);

                        previousTocEntry = TableOfContentsHelper.getTailField(tocentry);
                    }
                } else {
                    const tocentry = TableOfContentsHelper.buildRecursivelySection({
                        item: section,
                        previousTocEntry,
                        path: [...path, item.id],
                        fieldSetFormGroup,
                        formGroup
                    }); // the nested fieldsets inherit the previousTocEntry
                    if (tocentry) {

                        // explanation: previous-section[last-field].next = current-section[first-field] 
                        const firstTocentryOfCurrentSection = TableOfContentsHelper.getHeadField(tocentry);
                        if (tempResult.length > 0) {
                            tempResult[tempResult.length - 1] = TableOfContentsHelper.setTocEntryNext(tempResult[tempResult.length - 1], firstTocentryOfCurrentSection);
                        }
                        
                        tempResult.push(tocentry);
                
                        previousTocEntry = TableOfContentsHelper.getTailField(tocentry);
                    }
                }
            });

        } else if (fieldsets && fieldsets.length) {

            fieldsets.forEach(fieldset => {
                if (previousTocEntry == null) {
                    const tocentry = TableOfContentsHelper.buildRecursivelyFieldSet({
                        item: fieldset,
                        path: [...path, item.id],
                        fieldSetFormGroup,
                        formGroup
                    });

                    if (tocentry != null) {
                        tempResult.push(tocentry);

                        previousTocEntry = tocentry;
                    }
                } else {			
                    const tocentry = TableOfContentsHelper.buildRecursivelyFieldSet({
                        item: fieldset,
                        previousEntry: previousTocEntry, path: [...path, item.id],
                        fieldSetFormGroup,
                        formGroup
                    });
                    if (tocentry) {
                        if (tempResult.length > 0) { 
                            tempResult[tempResult.length-1].nextEntry = tocentry;
                            tempResult[tempResult.length-1].isLastEntry = tocentry == null;
                        }
                        tempResult.push(tocentry);
                
                        previousTocEntry = tocentry;
                    }
                }
            });
        }
        return {
            id: item.id,
            label: item.title,
            numbering: '',
            subEntries: tempResult,
            subEntriesType: sections && sections.length ? ToCEntryType.Section : ToCEntryType.FieldSet,
            type: ToCEntryType.Section,
            ordinal: item.ordinal,
            visibilityRuleKey: item.id,
            validityAbstractControls: [fieldSetFormGroup?.get(item.id)].filter(x => !!x),
            pathToEntry: path
        }
    }

    export function buildRecursivelyFieldSet(params: {
        item: BaseFieldSet,
        previousEntry?: ToCEntry,
        nextEntry?: ToCEntry,
        path?: string[],
        fieldSetFormGroup: FormGroup<{[key: string]: FieldSetsFormGroup}> ,
        formGroup: FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>
    }): ToCEntry {
        const {item, previousEntry, nextEntry, path, fieldSetFormGroup, formGroup} = params;
        if (!item) return null;
        const systemControls = TableOfContentsHelper.getSystemFieldsValidityAbstractControls(item, formGroup);

        return {
            id: item.id,
            label: item.title,
            numbering: 's',
            subEntries: null,
            subEntriesType: ToCEntryType.Field,
            type: ToCEntryType.FieldSet,
            ordinal: item.ordinal,
            visibilityRuleKey: item.id,
            validityAbstractControls: [...systemControls, fieldSetFormGroup?.get(item.id)].filter(x => !!x),
            required: isFieldSetRequired(item),
            isLastEntry: nextEntry == null,
            isFirstEntry: previousEntry == null ,
            previousEntry: previousEntry,
            nextEntry: nextEntry,
            pathToEntry: path
        };
    }

    export function sortByOrdinal(tocentries: ToCEntry[]) {

        if (!tocentries || !tocentries.length) return;

        tocentries.sort(TableOfContentsHelper.customCompare);
        tocentries.forEach(entry => {
            TableOfContentsHelper.sortByOrdinal(entry.subEntries);
        });
    }

    export function customCompare(a, b) {
		return a.ordinal - b.ordinal;
	}

	export function calculateNumbering(tocentries: ToCEntry[], depth: number[] = []) {
		if (!tocentries || !tocentries.length) {
			return;
		}

		let prefixNumbering = depth.length ? depth.join('.') : '';

		if (depth.length) prefixNumbering = prefixNumbering + ".";
		tocentries.forEach((entry, i) => {
			entry.numbering = prefixNumbering + (i + 1);
			TableOfContentsHelper.calculateNumbering(entry.subEntries, [...depth, i + 1])
		});
	}

    export function getDescriptionTocEntries(params: {
        descriptionTemplate: DescriptionTemplate,
        fieldSetFormGroup: FormGroup<{[key: string]: FieldSetsFormGroup;}>,
        pathToEntry?: string[],
        formGroup: FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>
    }): ToCEntry[] {
        const {descriptionTemplate, fieldSetFormGroup, formGroup, pathToEntry = []} = params;
        if (descriptionTemplate == null) { return []; }
        const result: ToCEntry[] = [];

        const previousTocEntry: ToCEntry = null;

        //build parent sections
        descriptionTemplate.definition.sections.forEach((rootSection, i) => {
            let tocEntry = TableOfContentsHelper.buildRecursivelySection({
                item: rootSection,
                fieldSetFormGroup,
                formGroup,
                path: pathToEntry,
                previousTocEntry: null
            })

            tocEntry = {
                ...tocEntry,
                type: ToCEntryType.RootDescriptionSection,
                numbering: (i + 1).toString(),
            };
            // explanation: previous-page[last-section][last-field].next = current-page[first-section][first-field] 
            const firstTocentryOfCurrentPage = TableOfContentsHelper.getHeadField(tocEntry);
            if (result.length > 0) {
                result[result.length - 1] = TableOfContentsHelper.setTocEntryNext(result[result.length - 1], firstTocentryOfCurrentPage);
            }

            result.push(tocEntry);
        });
        TableOfContentsHelper.sortByOrdinal(result);
        //calculate numbering
        TableOfContentsHelper.calculateNumbering(result);
        return result;
    }

    export function getAllFieldSets(entries: ToCEntry[]): ToCEntry[] {

		const fieldsets: ToCEntry[] = [];
		if (!entries || !entries.length) return fieldsets;


		entries.forEach(e => {
			if (e.type === ToCEntryType.FieldSet) {
				fieldsets.push(e);
			} else {
				fieldsets.push(...TableOfContentsHelper.getAllFieldSets(e.subEntries));
			}
		});
		return fieldsets;
	}

    export function isTocEntryValid(entry: ToCEntry): boolean {
		if (entry == null) return true;

		if (entry.validityAbstractControls?.length && (
            entry.validityAbstractControls.every((x) => !x.touched) || entry.validityAbstractControls.every(x => x.disabled))){
            return true;
        }
		let currentValidity = entry.validityAbstractControls?.every((x) => x.valid) ?? true;
		if (!currentValidity) return currentValidity;
		entry.subEntries?.forEach(subEntry => {
			currentValidity = currentValidity && TableOfContentsHelper.isTocEntryValid(subEntry);
			if (!currentValidity){
                return currentValidity;
            }
		});

		return currentValidity;
	}

    export function getSystemFieldsValidityAbstractControls(
        fieldSet: BaseFieldSet,
        formGroup: FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>
    ): AbstractControl[] {
        const systemFields = fieldSet.fields?.filter(x => x.data?.fieldType == FieldType.SYSTEM);
        if(!systemFields?.length){ return []; }
        const res = [];
        systemFields.forEach((field) => {
            const control = TemplateFieldEditorModel.getSystemFieldControl(field, formGroup);
            if(control){
                res.push(control);
            }
        });
        return res.filter(x => !!x)
    }
}