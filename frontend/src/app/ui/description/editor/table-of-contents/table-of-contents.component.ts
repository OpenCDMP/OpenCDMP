import {Component, computed, effect, EventEmitter, Inject, input, OnDestroy, OnInit, Optional, Output, untracked} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop'
import { DescriptionTemplate, DescriptionTemplateFieldSet, DescriptionTemplateSection } from '@app/core/model/description-template/description-template';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { Observable, Subscription, debounceTime, distinctUntilChanged, filter, mergeMap, startWith, take, takeUntil } from 'rxjs';
import { ToCEntry } from './models/toc-entry';
import { ToCEntryType } from './models/toc-entry-type.enum';
import { TableOfContentsService } from './services/table-of-contents-service';
import { Guid } from '@common/types/guid';
import { PlanTempStorageService } from '@app/ui/plan/plan-editor-blueprint/plan-temp-storage.service';
import { PropertiesFormGroup } from '../description-editor.model';
import {
	FormAnnotationService,
	MULTI_FORM_ANNOTATION_SERVICE_TOKEN
} from "@app/ui/annotations/annotation-dialog-component/form-annotation.service";
import {MatDialog} from "@angular/material/dialog";
import {RouterUtilsService} from "@app/core/services/router/router-utils.service";

@Component({
    selector: 'app-table-of-contents',
    styleUrls: ['./table-of-contents.component.scss'],
    templateUrl: './table-of-contents.component.html',
    standalone: false
})
export class TableOfContentsComponent extends BaseComponent implements OnDestroy{

	@Output() entrySelected = new EventEmitter<any>();
	showErrors = input<boolean>(false);
    descriptionId = input<Guid>();
    isVisible = input<boolean>(false);
    selectedFieldId = input<string>();

    descriptionData = computed(() => this.planTempStorage.descriptions()?.get(this.descriptionId()?.toString()));
    formGroup = computed(() => this.descriptionData()?.formGroup);
    visibilityRulesService = computed(() => this.descriptionData()?.visibilityRulesService);

    private descriptionTemplate(id: Guid){
        return this.planTempStorage.getDescriptionTemplate(id);
    } 

	// formGroup = input<UntypedFormGroup>();
	// hasFocus = input<boolean>(false);
	// visibilityRulesService = input<VisibilityRulesService>();
	// descriptionTemplate = input<DescriptionTemplate>(null);
    ordinal = input<string>();


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


    templateValueChange$: Subscription;
	annotationsPerAnchor: Map<string, number> = new Map<string, number>();
	annotationsPerEntry = [];
	private formAnnotationService: FormAnnotationService

    notificationCount$: Subscription;
	constructor(
		private tableOfContentsService: TableOfContentsService,
        private planTempStorage: PlanTempStorageService,
		@Optional() @Inject(MULTI_FORM_ANNOTATION_SERVICE_TOKEN) private formAnnotationServices: FormAnnotationService[],
		protected dialog: MatDialog,
		public routerUtils: RouterUtilsService,
	) {
		super();
        effect(() => {
            if(!this.formGroup()){
                return;
            }
            const templateControl = this.formGroup()?.controls?.descriptionTemplateId;

            if(templateControl.value) {
                this.tocentries = this.getTocEntries(this.descriptionTemplate(templateControl.value));
				if(this.descriptionId()){
                    this.notificationCount$?.unsubscribe();
					this.formAnnotationService = this.formAnnotationServices?.find(service => service.getEntityId() === this.descriptionId());
					this.notificationCount$ = this.formAnnotationService?.getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
							this.annotationsPerAnchor = annotationsPerAnchor;
							if(this.annotationsPerAnchor){
								this.annotationsPerEntry = [];
								this.tocentries.forEach((value, key) => {
									this.computeCounts(value, Array.from(this.annotationsPerAnchor.keys()));
								})
							}
						});
				}
                if(untracked(this.selectedFieldId) && untracked(this.isVisible)){
                    this.selectField(untracked(this.selectedFieldId), false);
                }
                this._resetObserver();
            }else {
                this._cleanUpObserver();
            }

            this.templateValueChange$?.unsubscribe();
            this.templateValueChange$= templateControl.valueChanges.pipe(takeUntil(this._destroyed))
            .subscribe((newDescriptionTemplateId) => {
                if(newDescriptionTemplateId) {
                    this.tocentries = this.getTocEntries(this.descriptionTemplate(newDescriptionTemplateId));
                    this.onToCentrySelected(null);
                    this._resetObserver();
                }else {
                    this._cleanUpObserver();
                }
            })
        });
        effect(() => {
            const isVisible = this.isVisible();
            if(isVisible && this.tocentries?.length){
                this._resetObserver();
            } else {
                this._cleanUpObserver();
            }
        })
	}


	// ngOnInit(): void {
	// 	this.tableOfContentsService.getNextClickedEventObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
	// 		let next: ToCEntry;

	// 		if (!this.tocentrySelected) next = this._getHeadField(this.tocentries[0]);
	// 		else if (this.tocentrySelected?.subEntries && this.tocentrySelected?.subEntries.length > 0) {
	// 			next = this.tocentrySelected;
	// 			while (next?.subEntries && next?.subEntries?.length > 0 && !next?.nextEntry) {
	// 				next = next?.subEntries[0];
	// 			}
	// 		}
	// 		else next = this.tocentrySelected?.nextEntry;

	// 		while (!this._isVisible(next?.id) && !next?.isLastEntry) {
	// 			next = next?.nextEntry;
	// 		}

	// 		if (!next || !this._isVisible(next?.id)) {
	// 			this.onToCentrySelected(null);
	// 		} else {
	// 			this.onToCentrySelected(next);
	// 		}
	// 	});

	// 	this.tableOfContentsService.getPreviousEventObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
	// 		let previous: ToCEntry;

	// 		if (!this.tocentrySelected) previous = this._getHeadField(this.tocentries[0]);
	// 		else if (this.tocentrySelected?.subEntries && this.tocentrySelected?.subEntries.length > 0) {
	// 			previous = this.tocentrySelected;
	// 			while (previous?.subEntries && previous?.subEntries?.length > 0 && !previous?.previousEntry) {
	// 				previous = previous?.subEntries[0];
	// 			}
	// 			previous = previous.previousEntry;
	// 		}
	// 		else previous = this.tocentrySelected?.previousEntry;

	// 		while (!this._isVisible(previous?.id) && !previous?.isFirstEntry) {
	// 			previous = previous.previousEntry;
	// 		}

	// 		if (!previous || !this._isVisible(previous?.id)) {
	// 			this.onToCentrySelected(null);
	// 		} else {
	// 			this.onToCentrySelected(previous);
	// 		}
	// 	});
	// }

    public selectPrevious(){
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
    }

    public selectNext(){
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
            return;
        } else {
            this.onToCentrySelected(next);
        }
    }

    
    public selectField(fieldId: string, execute: boolean = true){
        if(!fieldId) { 
            this.onToCentrySelected(null);
            return;
        }
        const tocEntry = TableOfContentsComponent._findTocEntryById(fieldId, this.tocentries);
        if(tocEntry){
            this.onToCentrySelected(tocEntry, execute);
        }
    }

    private _cleanUpObserver(){
        if (this._intersectionObserver) {
			this._intersectionObserver.disconnect();
			this._intersectionObserver = null;
		}
    }

	private _resetObserver() {
		this._cleanUpObserver();

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
			if (!this.pauseIntersectionObserver) {
				const target_id = x.target.id;
				if (this._isVisible(target_id)) {
					this.tocentrySelected = TableOfContentsComponent._findTocEntryById(target_id, this.tocentries);
                    this.entrySelected.emit({entry: this.tocentrySelected,  execute: false})
				}
			}
		});
	}

	private _buildRecursivelySection(params: {item: DescriptionTemplateSection, previousTocEntry?: ToCEntry, path: string[]}): ToCEntry {
        let {item, previousTocEntry, path} = params;
		if (!item) return null;

		const sections = item.sections;
		const fieldsets = item.fieldSets;

		const tempResult: ToCEntry[] = [];


		if (sections && sections.length) {
			sections.forEach(section => {
				if (previousTocEntry == null) {
					let tocentry = this._buildRecursivelySection({item: section, path: [...path, item.id]});
					if (tocentry != null) {
						tempResult.push(tocentry);

						previousTocEntry = this._getTailField(tocentry);
					}
				} else {
					let tocentry = this._buildRecursivelySection({item: section, previousTocEntry, path: [...path, item.id]}); // the nested fieldsets inherit the previousTocEntry
					if (tocentry) {

						// explanation: previous-section[last-field].next = current-section[first-field] 
						let firstTocentryOfCurrentSection = this._getHeadField(tocentry);
						if (tempResult.length > 0) {
							tempResult[tempResult.length - 1] = this._setTocEntryNext(tempResult[tempResult.length - 1], firstTocentryOfCurrentSection);
						}
						
						tempResult.push(tocentry);
				
						previousTocEntry = this._getTailField(tocentry);
					}
				}
			});

		} else if (fieldsets && fieldsets.length) {

			fieldsets.forEach(fieldset => {
				if (previousTocEntry == null) {
					let tocentry = this._buildRecursivelyFieldSet({item: fieldset, path: [...path, item.id]});

					if (tocentry != null) {
						tempResult.push(tocentry);

						previousTocEntry = tocentry;
					}
				} else {			
					let tocentry = this._buildRecursivelyFieldSet({item: fieldset, previousEntry: previousTocEntry, path: [...path, item.id]});
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
			validityAbstractControl: (this.formGroup().controls.properties as PropertiesFormGroup)?.get(item.id),
            pathToEntry: path
		}
	}

	private _buildRecursivelyFieldSet(params: {item: DescriptionTemplateFieldSet, previousEntry?: ToCEntry, nextEntry?: ToCEntry, path?: string[]}): ToCEntry {
        const {item, previousEntry, nextEntry, path} = params;
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
			validityAbstractControl: (this.formGroup().controls.properties as PropertiesFormGroup)?.get('fieldSets')?.get(item.id),
			isLastEntry: nextEntry == null,
			isFirstEntry: previousEntry == null ,
			previousEntry: previousEntry,
			nextEntry: nextEntry,
            pathToEntry: path
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

		let previousTocEntry: ToCEntry = null;

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
				validityAbstractControl: null,
                pathToEntry: []
			};

			const sections = pageElement.sections;

			sections.forEach(section => {
				const tempResults = this._buildRecursivelySection({item: section, previousTocEntry, path: [pageElement.id]});

				// explanation: previous-section[last-field].next = current-section[first-field] 
				let firstTocentryOfCurrentSection = this._getHeadField(tempResults);
				if (tocEntry.subEntries.length > 0) {
					tocEntry.subEntries[tocEntry.subEntries.length - 1] = this._setTocEntryNext(tocEntry.subEntries[tocEntry.subEntries.length - 1], firstTocentryOfCurrentSection);
				}

				tocEntry.subEntries.push(tempResults);

				previousTocEntry = this._getTailField(tempResults);
			});
			
			// explanation: previous-page[last-section][last-field].next = current-page[first-section][first-field] 
			let firstTocentryOfCurrentPage = this._getHeadField(tocEntry);
			if (result.length > 0) {
				result[result.length - 1] = this._setTocEntryNext(result[result.length - 1], firstTocentryOfCurrentPage);
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

	private _setTocEntryNext(previous: ToCEntry, next: ToCEntry): ToCEntry {
		if (!previous) return null;

		if (previous.subEntries && previous.subEntries.length) {
			let last: number = previous.subEntries.length-1;
			previous.subEntries[last] = this._setTocEntryNext(previous.subEntries[last], next);
			return previous;
		}
		else {
			previous.nextEntry = next;
			previous.isLastEntry = next == null
			return previous;
		}
	}

	private _isVisible(entryId: string): boolean {
		return this.visibilityRulesService().isVisibleMap[entryId] ?? true;
	}
	computeCounts(entry, keys){
		if(keys.indexOf(entry.id)!=-1){
			this.annotationsPerEntry[entry.id] = this.annotationsPerAnchor.get(entry.id); // field
		}
			entry?.subEntries?.forEach(subEntry=>
			{
				this.computeCounts(subEntry, keys);
				if(this.annotationsPerEntry[subEntry.id]) {
					this.annotationsPerEntry[entry.id] = this.annotationsPerEntry[entry.id] ?
						this.annotationsPerEntry[entry.id] + this.annotationsPerEntry[subEntry.id] : this.annotationsPerEntry[subEntry.id];
				}
			})
	}

    ngOnDestroy(): void {
        this.notificationCount$?.unsubscribe();
        this.notificationCount$ = null;
        this.templateValueChange$?.unsubscribe();
        this.templateValueChange$ = null;
    }
}

export interface LinkToScroll {
	page: number;
	section: number;
}
