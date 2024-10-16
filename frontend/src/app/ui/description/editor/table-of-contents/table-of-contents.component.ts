import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplate, DescriptionTemplateFieldSet, DescriptionTemplateSection } from '@app/core/model/description-template/description-template';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { Observable, debounceTime, distinctUntilChanged, filter, mergeMap, take, takeUntil } from 'rxjs';
import { ToCEntry } from './models/toc-entry';
import { ToCEntryType } from './models/toc-entry-type.enum';
import { TableOfContentsService } from './services/table-of-contents-service';

@Component({
	selector: 'app-table-of-contents',
	styleUrls: ['./table-of-contents.component.scss'],
	templateUrl: './table-of-contents.component.html'
})
export class TableOfContentsComponent extends BaseComponent implements OnInit, OnChanges {

	@Output() entrySelected = new EventEmitter<any>();
	@Input() showErrors: boolean = false;
	@Input() formGroup: UntypedFormGroup;
	@Input() descriptionTemplate: DescriptionTemplate;
	@Input() hasFocus: boolean = false;
	@Input() visibilityRulesService: VisibilityRulesService;
	@Input() anchorFieldsetId: string;

	tocentries: ToCEntry[] = null;
	
	private _intersectionObserver: IntersectionObserver;
	pauseIntersectionObserver: boolean = false;

	private _tocentrySelected: ToCEntry = null;
	get tocentrySelected() {
		return this._tocentrySelected;
	}
	set tocentrySelected(value) {
		this._tocentrySelected = value;
	}


	constructor(
		private tableOfContentsService: TableOfContentsService,
	) {
		super();
	}


	ngOnInit(): void {

		if (this.descriptionTemplate) {
			this.tocentries = this.getTocEntries(this.descriptionTemplate);
			if (this.anchorFieldsetId) {
				const anchorTocentry = TableOfContentsComponent._findTocEntryById(this.anchorFieldsetId, this.tocentries);
				if (anchorTocentry) setTimeout(() => { this.onToCentrySelected(anchorTocentry) }, 300);
			}
		}

		this.tableOfContentsService.getNextClickedEventObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
			let next: ToCEntry;

			if (!this.tocentrySelected) next = this._getHeadField(this.tocentries[0]);
			else if (this.tocentrySelected?.subEntries && this.tocentrySelected?.subEntries.length > 0) {
				next = this.tocentrySelected;
				while (next?.subEntries && next?.subEntries?.length > 0 && !next?.nextEntry) {
					next = next?.subEntries[0];
				}
			}
			else next = this.tocentrySelected?.nextEntry;

			while (!this._isVisible(next?.id) && !next?.isLastEntry) {
				next = next?.nextEntry;
			}

			if (!next || !this._isVisible(next?.id)) {
				this.onToCentrySelected(null);
			} else {
				this.onToCentrySelected(next);
			}
		});

		this.tableOfContentsService.getPreviousEventObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
			let previous: ToCEntry;

			if (!this.tocentrySelected) previous = this._getHeadField(this.tocentries[0]);
			else if (this.tocentrySelected?.subEntries && this.tocentrySelected?.subEntries.length > 0) {
				previous = this.tocentrySelected;
				while (previous?.subEntries && previous?.subEntries?.length > 0 && !previous?.previousEntry) {
					previous = previous?.subEntries[0];
				}
				previous = previous.previousEntry;
			}
			else previous = this.tocentrySelected?.previousEntry;

			while (!this._isVisible(previous?.id) && !previous?.isFirstEntry) {
				previous = previous.previousEntry;
			}

			if (!previous || !this._isVisible(previous?.id)) {
				this.onToCentrySelected(null);
			} else {
				this.onToCentrySelected(previous);
			}
		});
	}

	ngOnChanges(changes: SimpleChanges) {

		if (changes['hasFocus'] && changes.hasFocus.currentValue) {
			this._resetObserver();
		}
		if (changes['descriptionTemplate'] && changes.descriptionTemplate != null) {
			this.tocentries = this.getTocEntries(this.descriptionTemplate);
		}
		if (changes['anchorFieldsetId'] && changes.anchorFieldsetId != null) {
			const anchorTocentry = TableOfContentsComponent._findTocEntryById(this.anchorFieldsetId, this.tocentries);
			if (anchorTocentry) setTimeout(() => { this.onToCentrySelected(anchorTocentry) }, 300);
		}
	}

	private _resetObserver() {
		if (this._intersectionObserver) {//clean up
			this._intersectionObserver.disconnect();
			this._intersectionObserver = null;
		}

		new Observable(observer => {
			const options = {
				root: null,
				rootMargin: '20% 0px -30% 0px',
				threshold: [0, 0.2, 0.5, 1]
			}
			this._intersectionObserver = new IntersectionObserver(entries => {
				observer.next(entries);
			}, options);

			const fieldsetsEtries = this._getAllFieldSets(this.tocentries);

			fieldsetsEtries.forEach(e => {
				if (e.type === ToCEntryType.FieldSet) {
					try {
						const targetElement = document.getElementById(e.id);
						this._intersectionObserver.observe(targetElement);
					} catch {
						console.log('element not found');
					}
				}
			});

			return () => { this._intersectionObserver.disconnect(); };
		}).pipe(
			takeUntil(this._destroyed),
			mergeMap((entries: IntersectionObserverEntry[]) => entries),
			filter(entry => entry.isIntersecting),
			debounceTime(300),
			distinctUntilChanged(),
		).subscribe(x => {
			if (x.isIntersecting && !this.pauseIntersectionObserver) {
				const target_id = x.target.id;
				if (this._isVisible(target_id)) {
					this.tocentrySelected = TableOfContentsComponent._findTocEntryById(target_id, this.tocentries);
				}
			}
		});
	}

	private _buildRecursivelySection(item: DescriptionTemplateSection, previousTocentry?: ToCEntry): ToCEntry {
		if (!item) return null;

		const sections = item.sections;
		const fieldsets = item.fieldSets;

		const tempResult: ToCEntry[] = [];

		if (sections && sections.length) {
			sections.forEach(section => {
				if (previousTocentry == null) {
					let tocentry = this._buildRecursivelySection(section);
					if (tocentry != null) {
						tempResult.push(tocentry);

						previousTocentry = this._getTailField(tocentry);
					}
				} else {
					let tocentry = this._buildRecursivelySection(section, previousTocentry); // the nested fieldsets inherit the previousTocEntry
					if (tocentry) {

						// explanation: previous-section[last-field].next = current-section[first-field] 
						let firstTocentryOfCurrentSection = this._getHeadField(tocentry);
						if (tempResult.length > 0) {
							tempResult[tempResult.length - 1] = this._setTocentryNext(tempResult[tempResult.length - 1], firstTocentryOfCurrentSection);
						}
						
						tempResult.push(tocentry);
				
						previousTocentry = this._getTailField(tocentry);
					}
				}
			});

		} else if (fieldsets && fieldsets.length) {

			fieldsets.forEach(fieldset => {
				if (previousTocentry == null) {
					let tocentry = this._buildRecursivelyFieldSet(fieldset);

					if (tocentry != null) {
						tempResult.push(tocentry);

						previousTocentry = tocentry;
					}
				} else {			
					let tocentry = this._buildRecursivelyFieldSet(fieldset, previousTocentry, null);
					if (tocentry) {
						if (tempResult.length > 0) { 
							tempResult[tempResult.length-1].nextEntry = tocentry;
							tempResult[tempResult.length-1].isLastEntry = tocentry == null;
						}
						tempResult.push(tocentry);
				
						previousTocentry = tocentry;
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
			validityAbstractControl: this.formGroup.get('fieldSets')?.get(item.id),
		}
	}

	private _buildRecursivelyFieldSet(item: DescriptionTemplateFieldSet, previousEntry?: ToCEntry, nextEntry?: ToCEntry): ToCEntry {
		if (!item) return null;

		return {
			id: item.id,
			label: item.title,
			numbering: 's',
			subEntries: null,
			subEntriesType: ToCEntryType.Field,
			type: ToCEntryType.FieldSet,
			ordinal: item.ordinal,
			visibilityRuleKey: item.id,
			validityAbstractControl: this.formGroup.get('fieldSets')?.get(item.id),
			isLastEntry: nextEntry == null,
			isFirstEntry: previousEntry == null ,
			previousEntry: previousEntry,
			nextEntry: nextEntry
		};
	}

	private _sortByOrdinal(tocentries: ToCEntry[]) {

		if (!tocentries || !tocentries.length) return;

		tocentries.sort(this._customCompare);
		tocentries.forEach(entry => {
			this._sortByOrdinal(entry.subEntries);
		});
	}

	private _customCompare(a, b) {
		return a.ordinal - b.ordinal;
	}

	private _calculateNumbering(tocentries: ToCEntry[], depth: number[] = []) {
		if (!tocentries || !tocentries.length) {
			return;
		}

		let prefixNumbering = depth.length ? depth.join('.') : '';

		if (depth.length) prefixNumbering = prefixNumbering + ".";
		tocentries.forEach((entry, i) => {
			entry.numbering = prefixNumbering + (i + 1);
			this._calculateNumbering(entry.subEntries, [...depth, i + 1])
		});
	}


	getTocEntries(descriptionTemplate: DescriptionTemplate): ToCEntry[] {
		if (descriptionTemplate == null) { return []; }
		const result: ToCEntry[] = [];

		let previousTocentry: ToCEntry = null;

		//build parent pages
		descriptionTemplate.definition.pages.forEach((pageElement, i) => {
			const tocEntry: ToCEntry = {
				id: pageElement.id,
				label: pageElement.title,
				type: ToCEntryType.Page,
				numbering: (i + 1).toString(),
				subEntriesType: ToCEntryType.Section,
				subEntries: [],
				ordinal: pageElement.ordinal,
				visibilityRuleKey: pageElement.id,
				validityAbstractControl: null
			};

			const sections = pageElement.sections;

			sections.forEach(section => {
				const tempResults = this._buildRecursivelySection(section, previousTocentry);

				// explanation: previous-section[last-field].next = current-section[first-field] 
				let firstTocentryOfCurrentSection = this._getHeadField(tempResults);
				if (tocEntry.subEntries.length > 0) {
					tocEntry.subEntries[tocEntry.subEntries.length - 1] = this._setTocentryNext(tocEntry.subEntries[tocEntry.subEntries.length - 1], firstTocentryOfCurrentSection);
				}

				tocEntry.subEntries.push(tempResults);

				previousTocentry = this._getTailField(tempResults);
			});
			
			// explanation: previous-page[last-section][last-field].next = current-page[first-section][first-field] 
			let firstTocentryOfCurrentPage = this._getHeadField(tocEntry);
			if (result.length > 0) {
				result[result.length - 1] = this._setTocentryNext(result[result.length - 1], firstTocentryOfCurrentPage);
			}

			result.push(tocEntry);
		});

		this._sortByOrdinal(result);
		//calculate numbering
		this._calculateNumbering(result);
		return result;

	}

	onToCentrySelected(entry: ToCEntry = null, execute: boolean = true) {
		this.pauseIntersectionObserver = true;
		this.tocentrySelected = entry;
		this.entrySelected.emit({ entry: entry, execute: execute });
		setTimeout(() => this.pauseIntersectionObserver = false, 2000);
	}

	/**
	 * Get all filedsets in a tocentry array;
	 * @param entries Tocentries to search in
	 * @returns The tocentries that are Fieldsets provided in the entries
	 */
	private _getAllFieldSets(entries: ToCEntry[]): ToCEntry[] {

		const fieldsets: ToCEntry[] = [];
		if (!entries || !entries.length) return fieldsets;


		entries.forEach(e => {
			if (e.type === ToCEntryType.FieldSet) {
				fieldsets.push(e);
			} else {
				fieldsets.push(...this._getAllFieldSets(e.subEntries));
			}
		});
		return fieldsets;
	}

	public static _findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
		if (!tocentries || !tocentries.length) {
			return null;
		}

		let tocEntryFound = tocentries.find(entry => entry.id === id);

		if (tocEntryFound) {
			return tocEntryFound;
		}

		for (let entry of tocentries) {
			const result = this._findTocEntryById(id, entry.subEntries);
			if (result) {
				tocEntryFound = result;
				break;
			}
		}

		return tocEntryFound ? tocEntryFound : null;
	}

	private _getHeadField(item: ToCEntry): ToCEntry {
		if (!item) return null;

		const subEntries = item.subEntries;

		if (subEntries && subEntries.length) return this._getHeadField(subEntries[0]);
		else return item;
	}
	
	private _getTailField(item: ToCEntry): ToCEntry {
		if (!item) return null;

		const subEntries = item.subEntries;

		if (subEntries && subEntries.length) return this._getTailField(subEntries[subEntries.length-1]);
		else return item;
	}

	private _setTocentryNext(previous: ToCEntry, next: ToCEntry): ToCEntry {
		if (!previous) return null;

		if (previous.subEntries && previous.subEntries.length) {
			let last: number = previous.subEntries.length-1;
			previous.subEntries[last] = this._setTocentryNext(previous.subEntries[last], next);
			return previous;
		}
		else {
			previous.nextEntry = next;
			previous.isLastEntry = next == null
			return previous;
		}
	}

	private _isVisible(entryId: string): boolean {
		return this.visibilityRulesService.isVisibleMap[entryId] ?? true;
	}
}

export interface LinkToScroll {
	page: number;
	section: number;
}
