import { UntypedFormArray, FormArray, FormGroup } from "@angular/forms";
import {  TemplateSectionForm } from "../description-template-editor.model";
import { ToCEntry, ToCEntryType } from "./description-template-table-of-contents-entry";
import { PlanBlueprintRootSectionForm } from "@app/ui/admin/plan-blueprint/editor/plan-blueprint-editor.model";

export namespace DefinitionTocEntryHelper {
    /**
     * 	Creates toc entries based on sections form array (either description template or plan blueprint form)
     *
     * @param sectionsFormArray
     *
     */
    export function getTocEntries(
        sectionsFormArray: FormArray<FormGroup<PlanBlueprintRootSectionForm>> | FormArray<FormGroup<TemplateSectionForm>>
    ): ToCEntry[] {
        if (sectionsFormArray == null) { return []; }
        const result: ToCEntry[] = [];

        sectionsFormArray.controls.forEach((sectionElement, i) => {
            const item: ToCEntry = {
                id: sectionElement.get('id').value,
                label: sectionElement.get('title').value,
                type: ToCEntryType.Section,
                form: sectionElement,
                numbering: (i + 1).toString(),
                validationRootPath: 'definition.sections[' + i + ']',
                subEntries: [],
                subEntriesType: null,
                root: true
            };
            const sectionItems = DefinitionTocEntryHelper.populateSections(sectionElement.get('sections') as UntypedFormArray, item.numbering, item.validationRootPath);
            const fieldSetItems = DefinitionTocEntryHelper.populateFieldSets(sectionElement.get('fieldSets') as UntypedFormArray, item.numbering, item.validationRootPath);

            if (sectionItems != null) {
                item.subEntries = sectionItems;
                item.subEntriesType = ToCEntryType.Section;
            }

            if (fieldSetItems != null) {
                if (item.subEntries == null) {
                    item.subEntries = fieldSetItems;
                } else {
                    item.subEntries.push(...fieldSetItems);
                }
                item.subEntriesType = ToCEntryType.FieldSet;

            }

            result.push(item)
        });

        DefinitionTocEntryHelper.sortToCentries(result);//ordeby ordinal
        DefinitionTocEntryHelper.updateNumbering(result, '');//update nubering if needed
        return result;
    }
    /**
     * 	Updates entries ordinal form value
     * based on the index they have on the tocentry array.
     * Tocentries that are on the same level have distinct ordinal value
     *
     * @param tocentries
     *
     */
    export function updateOrdinals(tocentries: ToCEntry[]) {

        if (!tocentries || !tocentries.length) return;
        tocentries.forEach((e, idx) => {
            const ordinalControl = e.form.get('ordinal');
            if (ordinalControl) {
                ordinalControl.setValue(idx);
                ordinalControl.updateValueAndValidity();
            }
            DefinitionTocEntryHelper.updateOrdinals(e.subEntries);
        });
    }

    export function getNextFieldAfterDeletingTocentry(tce: ToCEntry): any {
        const sameLevelFields: UntypedFormArray = tce.form?.parent?.parent?.get('fieldSets')?.value;

        let tceIndex = -1;

        //find tce index
        for (let i = 0; i < sameLevelFields.length; i++) {
            const section: any = sameLevelFields?.at(i);
            const sectionId = section?.id;
            if (sectionId == tce.id) {
                tceIndex = i;
                break;
            }
        }

        return sameLevelFields.at(tceIndex > 0 ? tceIndex - 1 : tceIndex + 1); //if deleting the first field, find the next one, else find the previous
    }

    export function populateSections(sections: UntypedFormArray, existingNumbering: string, validationRootPath: string): ToCEntry[] {
        if (sections == null || sections.controls == null || sections.controls.length == 0) { return null; }

        const result: ToCEntry[] = [];
        sections.controls.forEach((sectionElement, i) => {

            const item = {
                id: sectionElement.get('id').value,
                label: sectionElement.get('title').value,
                type: ToCEntryType.Section,
                form: sectionElement,
                numbering: existingNumbering + '.' + (i + 1),
                validationRootPath: validationRootPath + '.sections[' + i + ']'
            } as ToCEntry;
            const sectionItems = DefinitionTocEntryHelper.populateSections(sectionElement.get('sections') as UntypedFormArray, item.numbering, item.validationRootPath);
            const fieldSetItems = DefinitionTocEntryHelper.populateFieldSets(sectionElement.get('fieldSets') as UntypedFormArray, item.numbering, item.validationRootPath);
            if (sectionItems != null) {
                item.subEntries = sectionItems;
                item.subEntriesType = ToCEntryType.Section;
            }
            if (fieldSetItems != null) {
                if (item.subEntries == null) {
                    item.subEntries = fieldSetItems;
                } else {
                    item.subEntries.push(...fieldSetItems);
                }
                item.subEntriesType = ToCEntryType.FieldSet;
            }
            result.push(item);
        });

        return result;
    }

    export function populateFieldSets(fieldSets: UntypedFormArray, existingNumbering: string, validationRootPath: string): ToCEntry[] {
        if (fieldSets == null || fieldSets.controls == null || fieldSets.controls.length == 0) { return null; }

        const result: ToCEntry[] = [];
        fieldSets.controls.forEach((fieldSetElement, i) => {

            result.push({
                id: fieldSetElement.get('id').value,
                label: fieldSetElement.get('title').value,
                type: ToCEntryType.FieldSet,
                //subEntries: this.populateSections((fieldSetElement.get('fieldSets') as FormArray), existingNumbering + '.' + i),
                form: fieldSetElement,
                numbering: existingNumbering + '.' + (i + 1),
                validationRootPath: validationRootPath
            } as ToCEntry)

        });

        return result;
    }

    export function findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
        if (!tocentries || !tocentries.length) {
            return null;
        }

        let tocEntryFound = tocentries.find(entry => entry.id === id);

        if (tocEntryFound) {
            return tocEntryFound;
        }

        for (const entry of tocentries) {
            const result = DefinitionTocEntryHelper.findTocEntryById(id, entry.subEntries);
            if (result) {
                tocEntryFound = result;
                break;
            }
        }

        return tocEntryFound ? tocEntryFound : null;
    }

    export function findSectionIndex(sectionFormArray: UntypedFormArray, parentId: string): number[] {
        for (let i = 0; i < sectionFormArray?.length; i++) {
            const sectionFormGroup = sectionFormArray.at(i);
            const sectionId = sectionFormGroup.get('id').value;

            const parentSections = sectionFormGroup.get('sections') as UntypedFormArray;

            if (sectionId == parentId) {
                return [i];
            } else if (parentSections && parentSections.length > 0) {
                const indexes: number[] = DefinitionTocEntryHelper.findSectionIndex(parentSections, parentId);
                if (indexes && indexes.length > 0) {
                    indexes.unshift(i);
                    return indexes;
                }
            }
        }

        return null;
    }

    export function getUpdatedSectionFormArray(sectionFormArray: UntypedFormArray, tceId: string): UntypedFormArray {
        for (let i = 0; i < sectionFormArray?.length; i++) {
            const sectionFormGroup = sectionFormArray.at(i);
            const sectionId = sectionFormGroup.get('id').value;

            const parentSections = sectionFormGroup.get('sections') as UntypedFormArray;

            if (sectionId == tceId) {
                sectionFormArray.removeAt(i);
                return sectionFormArray;
                // sectionFormArray.at(i).get('ordinal').patchValue(i);
            } else if (parentSections && parentSections.length > 0) {
                const currentSectionFormArray = DefinitionTocEntryHelper.getUpdatedSectionFormArray(parentSections, tceId);
                if (currentSectionFormArray != null || currentSectionFormArray != undefined) {
                    return currentSectionFormArray;
                }
            }
        }

        return null;
    }

    export function updateSelectedItem(
        tce: ToCEntry,
        selectedTocEntry: ToCEntry,
        sections: FormArray<FormGroup<PlanBlueprintRootSectionForm>> | FormArray<FormGroup<TemplateSectionForm>>
    ) {

        if (selectedTocEntry) {

            if (DefinitionTocEntryHelper.tocEntryIsChildOf(selectedTocEntry, tce)) {
                // if (selectedTocEntry.type == ToCEntryType.Page) {
                // 	selectedTocEntry = null;
                // } else {

                //if first level section

                let isFirstLevel: boolean = false;
                for (let i = 0; i < sections?.length; i++) {
                    const section = sections.at(i);
                    const sectionId = section.controls.id.value;

                    if (sectionId == tce.id) {
                        isFirstLevel = true;
                        break;
                    }
                }

                const sameLevelFields: UntypedFormArray = tce.form?.parent?.parent?.get('fieldSets')?.value;
                if (sameLevelFields?.length > 1) {

                    const previousField = DefinitionTocEntryHelper.getNextFieldAfterDeletingTocentry(tce);
                    const tocentries = DefinitionTocEntryHelper.getTocEntries(sections);
                    const previousFieldTocEntry = DefinitionTocEntryHelper.findTocEntryById(previousField?.id, tocentries);

                    selectedTocEntry = previousFieldTocEntry;

                } else if (sameLevelFields?.length == 1) {
                    //scroll to parent

                    let parentId = null;
                    if (isFirstLevel) {
                        parentId = tce.form.get('page').value;
                    } else {
                        parentId = tce.form.parent.parent.get('id').value
                    }

                    if (parentId) {
                        const tocentries = DefinitionTocEntryHelper.getTocEntries(sections);
                        const parent = DefinitionTocEntryHelper.findTocEntryById(parentId, tocentries);

                        if (parent) {
                            selectedTocEntry = parent;
                        } else {
                            selectedTocEntry = null;
                        }
                    } else {
                        selectedTocEntry = null;
                    }
                } else {
                    selectedTocEntry = null;
                }
                // }
            }
            return selectedTocEntry;
        }
    }

    export function tocEntryIsChildOf(testingChild: ToCEntry, parent: ToCEntry): boolean {

        if (!testingChild || !parent) return false;

        if (testingChild.id == parent.id) { return true; }

        if (parent.subEntries) {
            let childFound: boolean = false;

            parent.subEntries.forEach(subEntry => {
                if (DefinitionTocEntryHelper.tocEntryIsChildOf(testingChild, subEntry)) {
                    childFound = true;
                    return true;
                }
            })

            return childFound;
        }
        return false;
    }

    /**
     * Get all filedsets in a tocentry array;
     * @param entries Tocentries to search in
     * @returns The tocentries that are Fieldsets provided in the entries
     */
    export function getAllFieldSets(entries: ToCEntry[]): ToCEntry[] {

        const fieldsets: ToCEntry[] = [];
        if (!entries || !entries.length) return fieldsets;


        entries.forEach(e => {
            if (e.type === ToCEntryType.FieldSet) {
                fieldsets.push(e);
            } else {
                fieldsets.push(...DefinitionTocEntryHelper.getAllFieldSets(e.subEntries));
            }
        });
        return fieldsets;
    }

    export function sortToCentries(entries: ToCEntry[]) {
        if (!entries || !entries.length) return;
        entries.sort(DefinitionTocEntryHelper.compareOrdinals);
        entries.forEach(e => {
            DefinitionTocEntryHelper.sortToCentries(e.subEntries)
        });
    }
    export function compareOrdinals(a, b) {

        const aValue = a.form.get('ordinal').value as number;
        const bValue = b.form.get('ordinal').value as number;

        return aValue - bValue;
    }
    export function updateNumbering(entries: ToCEntry[], parentNumbering: string) {
        if (!entries || !entries.length) return;
        let prefix = '';
        if (parentNumbering.length) {
            prefix = parentNumbering + '.';
        }
        entries.forEach((entry, index) => {
            const numbering = prefix + (index + 1);
            entry.numbering = numbering;
            DefinitionTocEntryHelper.updateNumbering(entry.subEntries, numbering);
        })
    }

}