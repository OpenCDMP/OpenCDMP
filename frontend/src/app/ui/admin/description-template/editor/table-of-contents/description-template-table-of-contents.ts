import { DOCUMENT } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { UntypedFormArray } from '@angular/forms';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { DragulaService } from 'ng2-dragula';
import { Subscription, interval } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { NewEntryType, TableUpdateInfo, ToCEntry, ToCEntryType } from './description-template-table-of-contents-entry';


@Component({
	selector: 'description-template-table-of-contents',
	styleUrls: ['./description-template-table-of-contents.scss'],
	templateUrl: './description-template-table-of-contents.html',
})
export class DescriptionTemplateTableOfContents extends BaseComponent implements OnInit, AfterViewInit, OnDestroy {

	@Input() links: ToCEntry[];
	@Input() itemSelected: ToCEntry;
	@Input() colorizeInvalid: boolean = false;
	@Input() viewOnly: boolean;
	@Input() showErrors: boolean = false;

	@Output() itemClick = new EventEmitter<ToCEntry>();
	@Output() removeEntry = new EventEmitter<ToCEntry>();
	@Output() createEntry = new EventEmitter<NewEntryType>();
	@Output() dataNeedsRefresh = new EventEmitter<TableUpdateInfo>();

	isDragging: boolean = false;
	draggingItemId: string = null;
	tocEntryType = ToCEntryType;
	dragSubscriptions: Subscription[] = [];
	DRAG_GROUP: string = "TABLEDRAG";
	DRAGULA_ITEM_ID_PREFIX = "table_item_id_";
	ROOT_ID: string = "ROOT_ID";//no special meaning
	private _dragStartedAt;
	private VALID_DROP_TIME = 500;//ms
	overcontainer: string = null;
		
	$clock = interval(10);
	scrollTableTop = false;
	scrollTableBottom = false;
	pxToScroll = 15;

	constructor(
		@Inject(DOCUMENT) private _document: Document,
		private dragulaService: DragulaService,
		private snackbar: MatSnackBar,
		private language: TranslateService
	) {
		super();

		this.dragSubscriptions.push(
			dragulaService.drag(this.DRAG_GROUP).subscribe(({ el }) => {
				this._dragStartedAt = new Date().getTime();
				this.isDragging = true;
				this.draggingItemId = (el.id as string).replace(this.DRAGULA_ITEM_ID_PREFIX, '');
			})
		);

		this.dragSubscriptions.push(
			dragulaService.drop(this.DRAG_GROUP).subscribe(({ el, target, source, sibling }) => {
				if (this._dragStartedAt) {
					const timeNow = new Date().getTime();
					if (timeNow - this._dragStartedAt > this.VALID_DROP_TIME) {
						this._dragStartedAt = null;

					} else {
						const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.GENERIC-GRAD-AND-DROP-ERROR');
						this.notifyUser(message);
						this.dataNeedsRefresh.emit();// even though the data is not changed the TABLE DRAG may has changed
						dragulaService.find(this.DRAG_GROUP).drake.cancel(true);
						return;
					}
				} else {
					const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.GENERIC-GRAD-AND-DROP-ERROR');
					this.notifyUser(message);
					this.dataNeedsRefresh.emit();// even though the data is not changed the TABLE DRAG may has changed
					dragulaService.find(this.DRAG_GROUP).drake.cancel(true);
					return;
				}

				const elementId = (el.id as string).replace(this.DRAGULA_ITEM_ID_PREFIX, '');
				const targetId = target.id as string;
				const sourceId = source.id as string;


				if (!(elementId && targetId && sourceId)) {
					const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.GENERIC-GRAD-AND-DROP-ERROR');
					this.notifyUser(message);
					this.dataNeedsRefresh.emit();
					dragulaService.find(this.DRAG_GROUP).drake.cancel(true);
					return;
				}

				const element: ToCEntry = this._findTocEntryById(elementId, this.links);
				const targetContainer: ToCEntry = this._findTocEntryById(targetId, this.links);
				const sourceContainer: ToCEntry = this._findTocEntryById(sourceId, this.links);
				if (!(element && (targetContainer || ((element.type === ToCEntryType.Page) && (targetId === this.ROOT_ID))) && (sourceContainer || ((element.type === ToCEntryType.Page) && (sourceId === this.ROOT_ID))))) {
					const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.GENERIC-GRAD-AND-DROP-ERROR');
					this.notifyUser(message);
					this.dataNeedsRefresh.emit();
					dragulaService.find(this.DRAG_GROUP).drake.cancel(true);
					return;
				}


				switch (element.type) {
					case ToCEntryType.FieldSet: {
						if (targetContainer.type != this.tocEntryType.Section) {
							const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.FIELDSET-MUST-HAVE-PARENT-SECTION');
							this.notifyUser(message)
							this.dataNeedsRefresh.emit();
							return;
						}

						//check if target container has no sections
						if ((targetContainer.form.get('sections') as UntypedFormArray).length) {
							const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.INPUT-SECTION-SAME-LEVEL');
							this.notifyUser(message);
							this.dataNeedsRefresh.emit();
							return;
						}

						const fieldsetForm = element.form;
						const targetFieldsets = targetContainer.form.get('fieldSets') as UntypedFormArray;
						const sourceFieldsets = sourceContainer.form.get('fieldSets') as UntypedFormArray;

						if (!targetFieldsets) {
							console.info('Not target fieldsets container found');
							this.dataNeedsRefresh.emit();
							return;
						}

						let sourceOrdinal = -1;
						let idx = -1;
						sourceFieldsets.controls.forEach((elem, index) => {
							if (elem.get('id').value === elementId) {
								sourceOrdinal = elem.get('ordinal').value;
								idx = index
							}
						});

						if (sourceOrdinal >= 0 && idx >= 0) {
							sourceFieldsets.removeAt(idx);

							sourceFieldsets.controls.forEach(control => {
								const ordinal = control.get('ordinal');
								if ((ordinal.value >= sourceOrdinal) && sourceOrdinal > 0) {
									const updatedOrdinalVal = ordinal.value - 1;
									ordinal.setValue(updatedOrdinalVal);
								}
							});
							sourceFieldsets.controls.sort(this._compareOrdinals);
						}

						let position: number = targetFieldsets.length;

						if (!sibling || !sibling.id) {
							console.info('No sibling Id found');
						} else {
							const siblingId = (sibling.id as string).replace(this.DRAGULA_ITEM_ID_PREFIX, '');
							let siblingIndex = -1;
							targetFieldsets.controls.forEach((e, idx) => {
								if (e.get('id').value === siblingId) {
									siblingIndex = idx;
									position = e.get('ordinal').value;
								}

							});

							if (siblingIndex >= 0) { //sibling found

								targetFieldsets.controls.filter(control => control.get('ordinal').value >= position).forEach(control => {
									const ordinal = control.get('ordinal');
									const updatedOrdinalVal = ordinal.value + 1;
									ordinal.setValue(updatedOrdinalVal);
								})
							}

						}


						fieldsetForm.get('ordinal').setValue(position);
						targetFieldsets.insert(position, fieldsetForm);
						targetFieldsets.controls.sort(this._compareOrdinals);
						this.dataNeedsRefresh.emit({ draggedItemId: elementId });

						break;
					}
					case ToCEntryType.Section: {

						if (targetContainer.type == ToCEntryType.Section) {
							if ((targetContainer.form.get('fieldSets') as UntypedFormArray).length) {
								const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.INPUT-SECTION-SAME-LEVEL');;
								this.notifyUser(message);
								this.dataNeedsRefresh.emit();
								return;
							}

							const targetSections = targetContainer.form.get('sections') as UntypedFormArray;
							const elementSectionForm = element.form;
							const sourceSections = elementSectionForm.parent as UntypedFormArray;

							if (!(targetSections && sourceSections && elementSectionForm)) {
								console.info('Could not load sections');
								this.dataNeedsRefresh.emit();
								return;
							}

							let idx = -1;
							sourceSections.controls.forEach((section, i) => {
								if (section.get('id').value === elementId) {
									idx = i;
								}
							});

							if (!(idx >= 0)) {
								console.info('Could not find element in Parent container');
								this.dataNeedsRefresh.emit();
								return;
							}

							sourceSections.controls.filter(control => control.get('ordinal').value >= elementSectionForm.get('ordinal').value).forEach(control => {
								const ordinal = control.get('ordinal');
								const updatedOrdinalVal = ordinal.value ? ordinal.value - 1 : 0;
								ordinal.setValue(updatedOrdinalVal);
							});


							sourceSections.removeAt(idx);

							let targetOrdinal = targetSections.length;

							if (sibling && sibling.id) {
								const siblingId = sibling.id.replace(this.DRAGULA_ITEM_ID_PREFIX, '');

								targetSections.controls.forEach((section, i) => {
									if (section.get('id').value === siblingId) {
										targetOrdinal = section.get('ordinal').value;
									}
								})

								targetSections.controls.filter(control => control.get('ordinal').value >= targetOrdinal).forEach(control => {
									const ordinal = control.get('ordinal');
									const updatedOrdinalVal = ordinal.value + 1;
									ordinal.setValue(updatedOrdinalVal);
								});

							} else {
								console.info('no siblings found');
							}
							elementSectionForm.get('ordinal').setValue(targetOrdinal);
							targetSections.insert(targetOrdinal, elementSectionForm);

						} else if (targetContainer.type === ToCEntryType.Page) {

							const rootform = targetContainer.form;
							const sectionForm = element.form;
							const parentSections = sectionForm.parent as UntypedFormArray;

							let parentIndex = -1;
							parentSections.controls.forEach((section, i) => {
								if (section.get('id')?.value === elementId) {
									parentIndex = i
								}
							})


							if (parentIndex < 0) {
								console.info('could not locate section in parents array');
								this.dataNeedsRefresh.emit();
								return;
							}

							//update parent sections ordinal
							parentSections.controls.filter(section => section.get('ordinal')?.value >= sectionForm.get('ordinal')?.value).forEach(section => {
								const ordinal = section.get('ordinal');
								const updatedOrdinalVal = ordinal?.value ? ordinal?.value - 1 : 0;
								ordinal?.setValue(updatedOrdinalVal);
							})

							parentSections.removeAt(parentIndex);



							let position = 0;
							if (targetContainer.subEntries) {
								position = targetContainer.subEntries.length;
							}
							//populate sections
							const targetSectionsArray = rootform.get('sections') as UntypedFormArray;


							if (sibling && sibling.id) {
								const siblingId = sibling.id.replace(this.DRAGULA_ITEM_ID_PREFIX, '');
								let indx = -1;

								targetContainer.subEntries.forEach((e, i) => {
									if (e.form.get('id')?.value === siblingId) {
										indx = i;
										position = e.form.get('ordinal')?.value;
									}
								});
								if (indx >= 0) {

									targetContainer.subEntries.filter(e => e.form?.get('ordinal').value >= position).forEach(e => {
										const ordinal = e.form.get('ordinal');
										const updatedOrdinalVal = ordinal?.value + 1;
										ordinal?.setValue(updatedOrdinalVal);
									});
								}

							} else {
								console.info('No sibling found');
							}

							sectionForm.get('ordinal')?.setValue(position);
							sectionForm.get('page')?.setValue(targetContainer.id);
							targetSectionsArray.push(sectionForm);

						} else {
							const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.DRAG-NOT-SUPPORTED');
							this.notifyUser(message);
							this.dataNeedsRefresh.emit();
							return;
						}



						this.dataNeedsRefresh.emit({ draggedItemId: elementId });
						break;
					}
					case ToCEntryType.Page: {
						if (targetId != this.ROOT_ID) {
							const message = this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TABLE-OF-CONTENTS.ERROR-MESSAGES.PAGE-ELEMENT-ONLY-TOP-LEVEL');
							this.notifyUser(message);
							this.dataNeedsRefresh.emit();
							return;
						}

						const rootForm = element.form.root;
						if (!rootForm) {
							console.info('Could not find root!')
							this.dataNeedsRefresh.emit();
							return;
						}


						const pages = rootForm.get('definition')?.get('pages') as UntypedFormArray;
						const pageForm = element.form;

						let index = -1;

						pages.controls.forEach((page, i) => {
							if (page.get('id').value === elementId) {
								index = i;
							}
						});

						if (index < 0) {
							console.info('Could not locate page on pages');
							this.dataNeedsRefresh.emit();
							return;
						}


						//ordinality
						pages.controls.filter(page => page.get('ordinal').value >= pageForm.get('ordinal').value).forEach(page => {
							const ordinal = page.get('ordinal');
							const ordinalVal = ordinal.value ? ordinal.value - 1 : 0;
							ordinal.setValue(ordinalVal);
						});

						pages.removeAt(index);

						let targetPosition = pages.length;

						if (sibling) {
							const siblingId = sibling.id.replace(this.DRAGULA_ITEM_ID_PREFIX, '');

							pages.controls.forEach((page, i) => {
								if (page.get('id').value === siblingId) {
									targetPosition = page.get('ordinal').value;
								}
							});
						}
						pageForm.get('ordinal').setValue(targetPosition);

						//update ordinality
						pages.controls.filter(page => page.get('ordinal').value >= targetPosition).forEach(page => {
							const ordinal = page.get('ordinal');
							const ordinalVal = ordinal.value + 1;
							ordinal.setValue(ordinalVal);
						});


						pages.insert(targetPosition, pageForm);
						this.dataNeedsRefresh.emit({ draggedItemId: elementId });
						break;
					}
					default:

						console.info('Could not support moving objects for specific type of element');
						this.dataNeedsRefresh.emit();
						return;

				}
			})
		);

		this.dragSubscriptions.push(
			dragulaService.over(this.DRAG_GROUP).subscribe(({ el, container, source }) => {
				try {
					this.overcontainer = container.id;
				} catch (error) {
					this.overcontainer = null;
				}
			})
		);

		this.dragSubscriptions.push(
			dragulaService.dragend(this.DRAG_GROUP).subscribe(({ el }) => {
				this.isDragging = false;
				this.draggingItemId = null;
				this.overcontainer = null;
			})
		);
	}

	ngOnInit(): void { }

	ngAfterViewInit(): void {

		const top = document.querySelector('.top-scroller');
		const bottom = document.querySelector('.bottom-scroller');
		const tableDiv = document.querySelector('#tocentrytable');

		try {
			top.addEventListener('mouseover', (e) => { this.scrollTableTop = true; }, {
				passive: true
			});
			bottom.addEventListener('mouseover', (e) => { this.scrollTableBottom = true; }, {
				passive: true
			});

			top.addEventListener('mouseout', (e) => { this.scrollTableTop = false }, {
				passive: true
			});
			bottom.addEventListener('mouseout', (e) => { this.scrollTableBottom = false; }, {
				passive: true
			});


			this.$clock
				.pipe(
					takeUntil(this._destroyed),
					filter(() => this.scrollTableTop)
				)
				.subscribe(() => {
					try {
						tableDiv.scrollBy(0, -this.pxToScroll);
					} catch { }
				});
			this.$clock
				.pipe(
					takeUntil(this._destroyed),
					filter(() => this.scrollTableBottom)
				)
				.subscribe(() => {
					try {
						tableDiv.scrollBy(0, this.pxToScroll);
					} catch { }
				});
		} catch {
			console.log('could not find scrolling elements');
		}


	}

	ngOnDestroy(): void {
		this.dragSubscriptions.forEach(subscription => subscription.unsubscribe())
	}

	private _findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
		if (!tocentries) {
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

	itemClicked(item: ToCEntry) {
		//leaf node
		this.itemClick.emit(item);
	}

	deleteEntry(currentLink: ToCEntry) {
		this.removeEntry.emit(currentLink);
	}

	createNewEntry(newEntry: NewEntryType) {
		this.createEntry.emit(newEntry);
	}
	onDataNeedsRefresh() {
		this.dataNeedsRefresh.emit();
	}

	notifyUser(message: string) {
		this.snackbar.open(message, null, this._snackBarConfig);
	}

	private _snackBarConfig: MatSnackBarConfig = {
		duration: 2000
	}

	private _compareOrdinals(a, b) {

		const aValue = a.get('ordinal').value as number;
		const bValue = b.get('ordinal').value as number;

		return aValue - bValue;
	}
}
