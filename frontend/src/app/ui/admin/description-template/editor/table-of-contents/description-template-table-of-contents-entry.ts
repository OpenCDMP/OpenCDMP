import { AbstractControl } from "@angular/forms";

export interface ToCEntry {
	id: string;
	label: string;
	subEntriesType: ToCEntryType;
	subEntries: ToCEntry[];
	type: ToCEntryType;
	form: AbstractControl;
	numbering: string;

	validationRootPath: string;
}


export enum ToCEntryType {
	Page = 0,
	Section = 1,
	FieldSet = 2,
	Field = 3
}

export interface NewEntryType {
	childType: ToCEntryType,
	parent: ToCEntry
}

export interface TableUpdateInfo{
	draggedItemId?: string;
	data?:any;
}