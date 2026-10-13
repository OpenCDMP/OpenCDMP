import {Component, computed, effect, EventEmitter, Input, input, OnDestroy, Output, ViewChild, ChangeDetectionStrategy, forwardRef} from '@angular/core';
import { BaseComponent } from '@common/base/base.component';
import { Observable, Subscription, debounceTime, takeUntil } from 'rxjs';
import { ToCEntry } from './models/toc-entry';
import { ToCEntryType } from './models/toc-entry-type.enum';
import { Guid } from '@common/types/guid';
import { PlanTempStorageService } from '@app/ui/plan/plan-editor-blueprint/plan-temp-storage.service';
import {MatDialog} from "@angular/material/dialog";
import {RouterUtilsService} from "@app/core/services/router/router-utils.service";
import { PlanWebSocketService } from '@app/core/services/websocket/plan-websocket.service';
import { TableOfContentsInternal } from './table-of-contents-internal/table-of-contents-internal';
import { PlanAnnotationService } from '@app/ui/plan/plan-editor-blueprint/plan-annotation.service';
import { TableOfContentsHelper } from './helpers/table-of-contents.helper';

@Component({
    selector: 'app-description-table-of-contents',
    styleUrls: ['./table-of-contents.component.scss'],
    templateUrl: './table-of-contents.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [forwardRef(() => TableOfContentsInternal)]
})
export class TableOfContentsComponent extends BaseComponent implements OnDestroy{
    @Input() id: string; 
    @ViewChild('internalTable') internalTable: TableOfContentsInternal;
    tocEntries = input<ToCEntry[]>([]);
	@Output() entrySelected = new EventEmitter<any>();
	showErrors = input<boolean>(false);
    descriptionId = input<Guid>();
    isVisible = input<boolean>(false);
    selected = input<ToCEntry>();

    descriptionData = computed(() => this.planTempStorage.descriptions()?.get(this.descriptionId()?.toString()));
    formGroup = computed(() => this.descriptionData()?.formGroup);
    visibilityRulesService = computed(() => this.descriptionData()?.visibilityRulesService);

    // private descriptionTemplate(id: Guid){
    //     return this.planTempStorage.getDescriptionTemplate(id);
    // } 

	// formGroup = input<UntypedFormGroup>();
	// hasFocus = input<boolean>(false);
	// visibilityRulesService = input<VisibilityRulesService>();
	// descriptionTemplate = input<DescriptionTemplate>(null);
    numbering = input<string>();

    annotationsPerEntry = input<Map<string, number>>(new Map<string, number>([]));

	
	private _intersectionObserver: IntersectionObserver;
	pauseIntersectionObserver: boolean = false;
	// Currently-intersecting fieldset elements -> their top offset, used to pick the top-most one.
	private _intersectingTops = new Map<string, number>();
	private _lastSpyId: string = null;
	// Click lock: after a TOC click we hold the selection (the clicked entry may not reach the top of
	// the viewport near the document end, so "top-most" would wrongly resolve to an earlier section).
	// Released only by a genuine user scroll gesture — not a timer, and kept separate from
	// pauseIntersectionObserver so the selection effect can't clear it.
	private _spyLocked: boolean = false;

	// private _tocentrySelected: ToCEntry = null;
	// get tocentrySelected() {
	// 	return this._tocentrySelected;
	// }
	// set tocentrySelected(value) {
	// 	this._tocentrySelected = value;
	// }


    templateValueChange$: Subscription;
	// annotationsPerAnchor: Map<string, number> = new Map<string, number>();
	// annotationsPerEntry = [];

    public totalAnnotations: number = 0;

    notificationCount$: Subscription;
	constructor(
        private planTempStorage: PlanTempStorageService,
		private planWebSocketService: PlanWebSocketService,
		protected dialog: MatDialog,
		public routerUtils: RouterUtilsService,
        private planAnnotationService: PlanAnnotationService
	) {
		super();
        effect(() => {
            if(this.selected() && this.isVisible()){
                this._resetObserver();
                this.pauseIntersectionObserver = false;
            }else{
                this.pauseIntersectionObserver = true;
            }
            // if(!this.formGroup()){
            //     return;
            // }
            // const templateControl = this.formGroup()?.controls?.descriptionTemplateId;

            // if(templateControl.value) {
            //     if(untracked(this.selected) && untracked(this.isVisible)){
            //         this.pauseIntersectionObserver = false;
            //         this.selectField(untracked(this.selected), false);
            //     } else {
            //         this.pauseIntersectionObserver = true;
            //     }
            //     this._resetObserver();
            // }else {
            //     this._cleanUpObserver();
            // }

            // this.templateValueChange$?.unsubscribe();
            // this.templateValueChange$= templateControl.valueChanges.pipe(takeUntil(this._destroyed))
            // .subscribe((newDescriptionTemplateId) => {
            //     if(newDescriptionTemplateId) {
            //         this.onToCentrySelected(null);
            //         this._resetObserver();
            //     }else {
            //         this._cleanUpObserver();
            //     }
            // })
        });
        effect(() => {
            const isVisible = this.isVisible();
            if(isVisible && this.tocEntries()?.length){
                setTimeout(() => {
                    this._resetObserver();
                })
            } else {
                // this.tocentrySelected = null;
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

    // public selectPrevious(){
    //     let previous: ToCEntry;

    //     if (!this.tocentrySelected) previous = TableOfContentsHelper.getHeadField(this.tocEntries()?.[0]);
    //     else if (this.tocentrySelected?.subEntries && this.tocentrySelected?.subEntries.length > 0) {
    //         previous = this.tocentrySelected;
    //         while (previous?.subEntries && previous?.subEntries?.length > 0 && !previous?.previousEntry) {
    //             previous = previous?.subEntries[0];
    //         }
    //         previous = previous.previousEntry;
    //     }
    //     else previous = this.tocentrySelected?.previousEntry;

    //     while (!this._isVisible(previous?.id) && !previous?.isFirstEntry) {
    //         previous = previous.previousEntry;
    //     }

    //     if (!previous || !this._isVisible(previous?.id)) {
    //         this.onToCentrySelected(null);
    //     } else {
    //         this.onToCentrySelected(previous);
    //     }
    // }

    // public selectNext(){
    //     let next: ToCEntry;

    //     if (!this.tocentrySelected) next = TableOfContentsHelper.getHeadField(this.tocEntries?.[0]);
    //     else if (this.tocentrySelected?.subEntries && this.tocentrySelected?.subEntries.length > 0) {
    //         next = this.tocentrySelected;
    //         while (next?.subEntries && next?.subEntries?.length > 0 && !next?.nextEntry) {
    //             next = next?.subEntries[0];
    //         }
    //     }
    //     else next = this.tocentrySelected?.nextEntry;

    //     while (!this._isVisible(next?.id) && !next?.isLastEntry) {
    //         next = next?.nextEntry;
    //     }

    //     if (!next || !this._isVisible(next?.id)) {
    //         return;
    //     } else {
    //         this.onToCentrySelected(next);
    //     }
    // }

    
    // public selectField(field: ToCEntry, execute: boolean = true){
    //     if(!field) { 
    //         this.onToCentrySelected(null);
    //         return;
    //     }
    //     const tocEntry = TableOfContentsHelper.findTocEntryById(field.id, this.tocEntries());
    //     if(tocEntry){
    //         this.onToCentrySelected(tocEntry, execute);
    //     }
    // }

    private _cleanUpObserver(){
        if (this._intersectionObserver) {
			this._intersectionObserver.disconnect();
			this._intersectionObserver = null;
		}
    }

	private _resetObserver() {
		this._cleanUpObserver();

		this._intersectingTops.clear();

		new Observable<void>(observer => {
			// Activation band = top ~40% of the viewport (no margin above the top, so a section
			// scrolled just past the top no longer counts and can't steal the "top-most" slot).
			const options = {
				root: null,
				rootMargin: '0px 0px -60% 0px',
				threshold: [0, 0.2, 0.5, 1]
			}
			this._intersectionObserver = new IntersectionObserver(entries => {
				// Maintain the set of currently-intersecting fieldsets with their viewport top, so we
				// can resolve the "active" entry as the top-most one rather than whichever fired last.
				entries.forEach(entry => {
					if (entry.isIntersecting) {
						this._intersectingTops.set(entry.target.id, entry.boundingClientRect.top);
					} else {
						this._intersectingTops.delete(entry.target.id);
					}
				});
				observer.next();
			}, options);

			const fieldsetsEtries = TableOfContentsHelper.getAllFieldSets(this.tocEntries());

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
			debounceTime(150),
		).subscribe(() => {
			if (this.pauseIntersectionObserver || this._spyLocked) { return; }
			// Active entry = the visible fieldset nearest the top of the viewport. This keeps the
			// selection stable when several fieldsets are on screen at once (e.g. right after a
			// click-scroll) and only advances when the next one actually reaches the top on scroll.
			let bestId: string = null;
			let bestTop = Number.POSITIVE_INFINITY;
			this._intersectingTops.forEach((top, id) => {
				if (this._isVisible(id) && top < bestTop) {
					bestTop = top;
					bestId = id;
				}
			});
			if (!bestId || bestId === this._lastSpyId) { return; }
			this._lastSpyId = bestId;
			const tocentrySelected = TableOfContentsHelper.findTocEntryById(bestId, this.tocEntries());
			if (tocentrySelected) {
				this.entrySelected.emit({ entry: tocentrySelected, execute: false });
			}
		});
	}

	// private _buildRecursivelySection(params: {item: BaseSection, previousTocEntry?: ToCEntry, path: string[]}): ToCEntry {
    //     let {item, previousTocEntry, path} = params;
	// 	if (!item) return null;

	// 	const sections = item.sections;
	// 	const fieldsets = item.fieldSets;

	// 	const tempResult: ToCEntry[] = [];


	// 	if (sections && sections.length) {
	// 		sections.forEach(section => {
	// 			if (previousTocEntry == null) {
	// 				let tocentry = this._buildRecursivelySection({item: section, path: [...path, item.id]});
	// 				if (tocentry != null) {
	// 					tempResult.push(tocentry);

	// 					previousTocEntry = this._getTailField(tocentry);
	// 				}
	// 			} else {
	// 				let tocentry = this._buildRecursivelySection({item: section, previousTocEntry, path: [...path, item.id]}); // the nested fieldsets inherit the previousTocEntry
	// 				if (tocentry) {

	// 					// explanation: previous-section[last-field].next = current-section[first-field] 
	// 					let firstTocentryOfCurrentSection = this._getHeadField(tocentry);
	// 					if (tempResult.length > 0) {
	// 						tempResult[tempResult.length - 1] = this._setTocEntryNext(tempResult[tempResult.length - 1], firstTocentryOfCurrentSection);
	// 					}
						
	// 					tempResult.push(tocentry);
				
	// 					previousTocEntry = this._getTailField(tocentry);
	// 				}
	// 			}
	// 		});

	// 	} else if (fieldsets && fieldsets.length) {

	// 		fieldsets.forEach(fieldset => {
	// 			if (previousTocEntry == null) {
	// 				let tocentry = this._buildRecursivelyFieldSet({item: fieldset, path: [...path, item.id]});

	// 				if (tocentry != null) {
	// 					tempResult.push(tocentry);

	// 					previousTocEntry = tocentry;
	// 				}
	// 			} else {			
	// 				let tocentry = this._buildRecursivelyFieldSet({item: fieldset, previousEntry: previousTocEntry, path: [...path, item.id]});
	// 				if (tocentry) {
	// 					if (tempResult.length > 0) { 
	// 						tempResult[tempResult.length-1].nextEntry = tocentry;
	// 						tempResult[tempResult.length-1].isLastEntry = tocentry == null;
	// 					}
	// 					tempResult.push(tocentry);
				
	// 					previousTocEntry = tocentry;
	// 				}
	// 			}
	// 		});
	// 	}
	// 	return {
	// 		id: item.id,
	// 		label: item.title,
	// 		numbering: '',
	// 		subEntries: tempResult,
	// 		subEntriesType: sections && sections.length ? ToCEntryType.Section : ToCEntryType.FieldSet,
	// 		type: ToCEntryType.Section,
	// 		ordinal: item.ordinal,
	// 		visibilityRuleKey: item.id,
	// 		validityAbstractControl: (this.formGroup().controls.properties as FormGroup<PropertiesFormGroup>)?.get(item.id),
    //         pathToEntry: path
	// 	}
	// }

	// private _buildRecursivelyFieldSet(params: {item: BaseFieldSet, previousEntry?: ToCEntry, nextEntry?: ToCEntry, path?: string[]}): ToCEntry {
    //     const {item, previousEntry, nextEntry, path} = params;
	// 	if (!item) return null;

	// 	return {
	// 		id: item.id,
	// 		label: item.title,
	// 		numbering: 's',
	// 		subEntries: null,
	// 		subEntriesType: ToCEntryType.Field,
	// 		type: ToCEntryType.FieldSet,
	// 		ordinal: item.ordinal,
	// 		visibilityRuleKey: item.id,
	// 		validityAbstractControl: (this.formGroup().controls.properties as FormGroup<PropertiesFormGroup>)?.get('fieldSets')?.get(item.id),
	// 		isLastEntry: nextEntry == null,
	// 		isFirstEntry: previousEntry == null ,
	// 		previousEntry: previousEntry,
	// 		nextEntry: nextEntry,
    //         pathToEntry: path
	// 	};
	// }

	// private _sortByOrdinal(tocentries: ToCEntry[]) {

	// 	if (!tocentries || !tocentries.length) return;

	// 	tocentries.sort(this._customCompare);
	// 	tocentries.forEach(entry => {
	// 		this._sortByOrdinal(entry.subEntries);
	// 	});
	// }

	// private _customCompare(a, b) {
	// 	return a.ordinal - b.ordinal;
	// }

	// private _calculateNumbering(tocentries: ToCEntry[], depth: number[] = []) {
	// 	if (!tocentries || !tocentries.length) {
	// 		return;
	// 	}

	// 	let prefixNumbering = depth.length ? depth.join('.') : '';

	// 	if (depth.length) prefixNumbering = prefixNumbering + ".";
	// 	tocentries.forEach((entry, i) => {
	// 		entry.numbering = prefixNumbering + (i + 1);
	// 		this._calculateNumbering(entry.subEntries, [...depth, i + 1])
	// 	});
	// }


	// getTocEntries(descriptionTemplate: DescriptionTemplate): ToCEntry[] {
	// 	if (descriptionTemplate == null) { return []; }
	// 	const result: ToCEntry[] = [];

	// 	let previousTocEntry: ToCEntry = null;

	// 	//build parent sections
	// 	descriptionTemplate.definition.sections.forEach((pageElement, i) => {
	// 		const tocEntry: ToCEntry = {
	// 			id: pageElement.id,
	// 			label: pageElement.title,
	// 			type: ToCEntryType.RootSection,
	// 			numbering: (i + 1).toString(),
	// 			subEntriesType: ToCEntryType.Section,
	// 			subEntries: [],
	// 			ordinal: pageElement.ordinal,
	// 			visibilityRuleKey: pageElement.id,
	// 			validityAbstractControl: null,
    //             pathToEntry: []
	// 		};

	// 		const sections = pageElement.sections;

	// 		sections.forEach(section => {
	// 			const tempResults = this._buildRecursivelySection({item: section, previousTocEntry, path: [pageElement.id]});

	// 			// explanation: previous-section[last-field].next = current-section[first-field] 
	// 			let firstTocentryOfCurrentSection = this._getHeadField(tempResults);
	// 			if (tocEntry.subEntries.length > 0) {
	// 				tocEntry.subEntries[tocEntry.subEntries.length - 1] = this._setTocEntryNext(tocEntry.subEntries[tocEntry.subEntries.length - 1], firstTocentryOfCurrentSection);
	// 			}

	// 			tocEntry.subEntries.push(tempResults);

	// 			previousTocEntry = this._getTailField(tempResults);
	// 		});
			
	// 		// explanation: previous-page[last-section][last-field].next = current-page[first-section][first-field] 
	// 		let firstTocentryOfCurrentPage = this._getHeadField(tocEntry);
	// 		if (result.length > 0) {
	// 			result[result.length - 1] = this._setTocEntryNext(result[result.length - 1], firstTocentryOfCurrentPage);
	// 		}

	// 		result.push(tocEntry);
	// 	});
	// 	this._sortByOrdinal(result);
	// 	//calculate numbering
	// 	this._calculateNumbering(result);
	// 	return result;
	// }

	onToCentrySelected(entry: ToCEntry = null, execute: boolean = true) {
		// Hold the clicked entry as the active one (and as the spy's current target) until the user
		// actually scrolls; a timer can't tell whether the smooth scroll reached the top.
		this._lastSpyId = entry?.id ?? this._lastSpyId;
		this._armSpyLock();
		this.entrySelected.emit({ entry: entry, execute: execute });
	}

	private _armSpyLock() {
		this._spyLocked = true;
		// Only unambiguous scroll gestures release the lock — not keys (the user is typing in fields).
		window.addEventListener('wheel', this._releaseSpyLock, { passive: true, capture: true });
		window.addEventListener('touchmove', this._releaseSpyLock, { passive: true, capture: true });
	}

	// Release the click lock on the first genuine user scroll gesture, then let the spy take over.
	private _releaseSpyLock = () => {
		if (!this._spyLocked) { return; }
		this._spyLocked = false;
		window.removeEventListener('wheel', this._releaseSpyLock, true);
		window.removeEventListener('touchmove', this._releaseSpyLock, true);
	};

	/**
	 * Get all filedsets in a tocentry array;
	 * @param entries Tocentries to search in
	 * @returns The tocentries that are Fieldsets provided in the entries
	 */
	// private _getAllFieldSets(entries: ToCEntry[]): ToCEntry[] {

	// 	const fieldsets: ToCEntry[] = [];
	// 	if (!entries || !entries.length) return fieldsets;


	// 	entries.forEach(e => {
	// 		if (e.type === ToCEntryType.FieldSet) {
	// 			fieldsets.push(e);
	// 		} else {
	// 			fieldsets.push(...TableOfContentsHelper.getAllFieldSets(e.subEntries));
	// 		}
	// 	});
	// 	return fieldsets;
	// }

	// public static _findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
	// 	if (!tocentries || !tocentries.length) {
	// 		return null;
	// 	}

	// 	let tocEntryFound = tocentries.find(entry => entry.id === id);

	// 	if (tocEntryFound) {
	// 		return tocEntryFound;
	// 	}

	// 	for (let entry of tocentries) {
	// 		const result = this._findTocEntryById(id, entry.subEntries);
	// 		if (result) {
	// 			tocEntryFound = result;
	// 			break;
	// 		}
	// 	}

	// 	return tocEntryFound ? tocEntryFound : null;
	// }

	// private _getHeadField(item: ToCEntry): ToCEntry {
	// 	if (!item) return null;

	// 	const subEntries = item.subEntries;

	// 	if (subEntries && subEntries.length) return this._getHeadField(subEntries[0]);
	// 	else return item;
	// }
	
	// private _getTailField(item: ToCEntry): ToCEntry {
	// 	if (!item) return null;

	// 	const subEntries = item.subEntries;

	// 	if (subEntries && subEntries.length) return this._getTailField(subEntries[subEntries.length-1]);
	// 	else return item;
	// }

	// private _setTocEntryNext(previous: ToCEntry, next: ToCEntry): ToCEntry {
	// 	if (!previous) return null;

	// 	if (previous.subEntries && previous.subEntries.length) {
	// 		let last: number = previous.subEntries.length-1;
	// 		previous.subEntries[last] = this._setTocEntryNext(previous.subEntries[last], next);
	// 		return previous;
	// 	}
	// 	else {
	// 		previous.nextEntry = next;
	// 		previous.isLastEntry = next == null
	// 		return previous;
	// 	}
	// }

	private _isVisible(entryId: string): boolean {
		return this.visibilityRulesService().isVisibleMap[entryId] ?? true;
	}
	// computeCounts(entry, keys){
	// 	if(keys.indexOf(entry.id)!=-1){
	// 		this.annotationsPerEntry[entry.id] = this.annotationsPerAnchor.get(entry.id); // field
	// 	}
	// 		entry?.subEntries?.forEach(subEntry=>
	// 		{
	// 			this.computeCounts(subEntry, keys);
	// 			if(this.annotationsPerEntry[subEntry.id]) {
	// 				this.annotationsPerEntry[entry.id] = this.annotationsPerEntry[entry.id] ?
	// 					this.annotationsPerEntry[entry.id] + this.annotationsPerEntry[subEntry.id] : this.annotationsPerEntry[subEntry.id];
	// 			}
	// 		})
	// }

    ngOnDestroy(): void {
        this.notificationCount$?.unsubscribe();
        this.notificationCount$ = null;
        this.templateValueChange$?.unsubscribe();
        this.templateValueChange$ = null;
        this._cleanUpObserver();
        this._releaseSpyLock();
    }

    public expandL1(){
        this.internalTable?.expandL1();
    }

    public expandAll(){
        this.internalTable?.expandAll();
    }

    public collapseAll(){
        this.internalTable?.collapseAll();
    }
}

export interface LinkToScroll {
	page: number;
	section: number;
}
