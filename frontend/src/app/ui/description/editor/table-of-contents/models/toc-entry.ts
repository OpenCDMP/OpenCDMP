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
	validityAbstractControls: AbstractControl[];
	/** Field-set entries: at least one of the set's fields is required (mirrors the canvas heading asterisk). */
	required?: boolean;

	isLastEntry?: boolean;
	isFirstEntry?: boolean;
	previousEntry?: ToCEntry;
	nextEntry?: ToCEntry;
    pathToEntry: string[]
}