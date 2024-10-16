import { TemplateRef, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { AutoCompleteGroup } from '../auto-complete-group';
import { OptionComponent } from './option-component.model';

export interface MultipleAutoCompleteConfiguration {
	// Delay for performing the request. Default: 200ms.
	requestDelay?: number;
	// Min characters for the filtering to be applied. Default: 3.
	minFilteringChars?: number;
	// Load and present items from start, without user query. Default: true.
	loadDataOnStart?: boolean;
	// Static or initial items.
	initialItems?: (excludedItems: any[], data?: any) => Observable<any[]>;
	// Data retrieval function
	filterFn?: (searchQuery: string, excludedItems: any[], extraData?: any) => Observable<any[]>;
	// Get selected items. Used when valueAssign is used so the full object can be retrieved for presentation.
	getSelectedItems?: (selectedItems: any[]) => Observable<any[]>;
	// Property formating for input
	displayFn?: (item: any) => string;
	// Function to group results in the drop down
	groupingFn?: (items: any[]) => AutoCompleteGroup[];
	// Display function for the drop down title
	titleFn?: (item: any) => string;
	// Display function for the drop down subtitle
	subtitleFn?: (item: any) => string;
	//Extra data passed to query function
	extraData?: any;
	// Callback to intercept value assignment based on item selection
	valueAssign?: (selectedItem: any) => any;
	// Callback to provide equals function betwen the values
	uniqueAssign?: (selectedItem: any) => any;
	// Get selected items. Used when valueAssign is used so the full object can be retrieved for presentation.
	canRemoveItem?: (selectedItem: any) => { canRemove: boolean, message: string };
	// Property formating template
	optionTemplate?: TemplateRef<any>;
	// Property formatting component
	optionComponent?: Type<OptionComponent>;
	// Selected value formating template
	selectedValueTemplate?: TemplateRef<any>;
	// Display icon that opens popup
	popupItemActionIcon?: string;


	autoSelectFirstOptionOnBlur?: boolean;

	appendClassToItem?: { class: string, applyFunc: (item: any) => boolean }[];

}
