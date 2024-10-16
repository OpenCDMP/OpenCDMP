import { AbstractControl } from "@angular/forms";
import { ToCEntryType } from "./toc-entry-type.enum";

export interface ToCEntry {
	id: string;
	label: string;
	subEntriesType: ToCEntryType;
	subEntries: ToCEntry[];
	type: ToCEntryType;
	numbering: string;
	ordinal: number;
	hidden?: boolean;
	visibilityRuleKey: string;
	validityAbstractControl: AbstractControl;

	isLastEntry?: boolean;
	isFirstEntry?: boolean;
	previousEntry?: ToCEntry;
	nextEntry?: ToCEntry;
}