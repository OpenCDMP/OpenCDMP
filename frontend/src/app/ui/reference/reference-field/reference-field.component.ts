import { Component, effect, EventEmitter, input, Input, Output, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';
import { Reference, ReferencePersist } from '@app/core/model/reference/reference';
import { Guid } from '@common/types/guid';
import { Subscription } from 'rxjs';
import { FormService } from '@common/forms/form-service';
import { ExternalFetcherBaseSourceConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';
import { ReferenceFieldInfoDialogComponent } from './info-dialog/reference-field-info-dialog.component';
import { ReferenceDialogEditorComponent } from './editor/reference-dialog-editor.component';
import { MatFormField, MatLabel, MatError, MatHint, MatPrefix } from '@angular/material/form-field';
import { MultipleAutoCompleteComponent } from '../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { SingleAutoCompleteComponent } from '../../../library/auto-complete/single/single-auto-complete.component';
import { MatIcon } from '@angular/material/icon';
import { ReferenceCardComponent } from '../reference-card/reference-card.component';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-reference-field-component',
    templateUrl: 'reference-field.component.html',
    styleUrls: ['./reference-field.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatLabel, MultipleAutoCompleteComponent, MatError, MatHint, SingleAutoCompleteComponent, MatIcon, MatPrefix, FormsModule, ReactiveFormsModule, ReferenceCardComponent, MatButton, TranslatePipe]
})
export class ReferenceFieldComponent extends BaseComponent {
    static nextId = 0

	sources = input<ExternalFetcherBaseSourceConfigurationPersist []>();
	dependencies = input<UntypedFormGroup>();
	referenceType = input<ReferenceType>();
	multiple = input<boolean>(true);
	definitionSourcekey = input<string>();
	testReferenceTypeId = input<Guid>();

	@Input() form: UntypedFormGroup = null;
	@Input() label: string = null;
	@Input() hideLabel: boolean = false;
	@Input() required: boolean = false;
	@Input() hint: string;
	@Input() placeholder: string;
    @Input() id: string = `multiple-autocomplete-${ReferenceFieldComponent.nextId++}`;
	@Output() selectedReference: EventEmitter<any> = new EventEmitter();

	// Rich option rendering for the inline autocomplete (external-source test mode dropdown).
	@ViewChild('optionCardTemplate', { static: true }) optionCardTemplate: TemplateRef<any>;

	referenceToUse: Reference[]= [];

	referenceDepedencyIds: Guid[] = [];

	multipleAutoCompleteSearchConfiguration: MultipleAutoCompleteConfiguration;
	singleAutoCompleteSearchConfiguration: SingleAutoCompleteConfiguration;

	dependenciesSubscription: Subscription = null;
	constructor(
		private referenceService: ReferenceService,
		public enumUtils: EnumUtils,
		public formService: FormService,
		private dialog: MatDialog,
	) {
        super();
        effect(() => {
            const referenceType = this.referenceType();
            const sources = this.sources();
            const dependencies = this.dependencies();
            // read so the effect runs again when they change
            this.multiple();
            this.definitionSourcekey();

            if(!referenceType && !sources){ return; }
            if(referenceType){
                this.referenceDepedencyIds = this.referenceType()?.definition?.sources?.filter(x => x.referenceTypeDependencies).flatMap(x => x.referenceTypeDependencies).filter(x => x).map(x => x.id);
                if (this.referenceDepedencyIds?.length > 0) {
                    this.resolveReferenceDepedency(true);
                    if(dependencies){
                        this.dependenciesSubscription?.unsubscribe();
                        this.dependenciesSubscription = this.dependencies().valueChanges.pipe(takeUntil(this._destroyed)).subscribe(changes => {
                            this.resolveReferenceDepedency(false);
                        });
                    }
                    return;
                }
            }
            if(referenceType || sources?.length){
                this.resetAutocompleteConfiguration();
            }
        })
    }

    resetAutocompleteConfiguration(){
        if (this.multiple()) { 
            this.singleAutoCompleteSearchConfiguration = null;
            this.multipleAutoCompleteSearchConfiguration = this.sources()?.length ? 
                this.referenceService.getMultipleAutoCompleteTestSearchConfiguration(this.referenceToUse, this.definitionSourcekey(), this.sources(), this.testReferenceTypeId()) :
                this.referenceService.getMultipleAutoCompleteSearchConfiguration(this.referenceType().id, this.referenceToUse);
            // Selected references render as expandable cards below the input (see template), so the
            // autocomplete's own chips are hidden via CSS. Dropdown options keep the rich card.
            if (this.multipleAutoCompleteSearchConfiguration) {
                this.multipleAutoCompleteSearchConfiguration.optionTemplate = this.optionCardTemplate;
            }
        } else {
            this.multipleAutoCompleteSearchConfiguration = null;
            this.singleAutoCompleteSearchConfiguration = this.sources()?.length ? 
                this.referenceService.getSingleAutocompleteTestSearchConfiguration(this.referenceToUse, this.definitionSourcekey(), this.sources(), this.testReferenceTypeId()) :
                this.referenceService.getSingleAutocompleteSearchConfiguration(this.referenceType().id, this.referenceToUse);
            if (this.singleAutoCompleteSearchConfiguration) {
                this.singleAutoCompleteSearchConfiguration.optionTemplate = this.optionCardTemplate;
            }
        }
    }

	// ngOnInit() {

	// 	this.referenceDepedencyIds = this.referenceType()?.definition?.sources?.filter(x => x.referenceTypeDependencies).flatMap(x => x.referenceTypeDependencies).filter(x => x).map(x => x.id);
	// 	if (this.referenceDepedencyIds && this.referenceDepedencyIds.length > 0) {
	// 		this.resolveReferenceDepedency(true);
    //         return;
	// 	}
    //     if(this.sources()?.length || this.referenceType()){
    //         if (this.multiple) { 
    //             this.multipleAutoCompleteSearchConfiguration = this.sources()?.length ? 
    //                 this.referenceService.getMultipleAutoCompleteTestSearchConfiguration(null, this.definitionSourcekey, this.sources()) :
    //                 this.referenceService.getMultipleAutoCompleteSearchConfiguration(this.referenceType().id, null);
    //         } else { 
    //             this.singleAutoCompleteSearchConfiguration = this.sources()?.length ? 
    //                 this.referenceService.getSingleAutocompleteTestSearchConfiguration(null, this.definitionSourcekey, this.sources()) :
    //                 this.referenceService.getSingleAutocompleteSearchConfiguration(this.referenceType().id, null);
    //         }

	// 	}
	// }

	// ngOnChanges(changes: SimpleChanges) {
	// 	if ((changes['dependencies'] || changes['referenceType']) && this.dependencies != null && this.referenceType() != null) {
	// 		this.referenceDepedencyIds = this.referenceType()?.definition?.sources?.filter(x => x.referenceTypeDependencies).flatMap(x => x.referenceTypeDependencies).filter(x => x).map(x => x.id);
	// 		if (this.referenceDepedencyIds && this.referenceDepedencyIds.length > 0) {
	// 			if (this.dependenciesSubscription != null){this.dependenciesSubscription.unsubscribe()};
	// 			this.resolveReferenceDepedency(true);
	// 			this.dependenciesSubscription = this.dependencies().valueChanges.pipe(takeUntil(this._destroyed)).subscribe(changes => {
	// 				this.resolveReferenceDepedency(false);
	// 			});
	// 		}
	// 	}
	// }

	resolveReferenceDepedency(isInitial: boolean) {
		const referenceToUse : Reference[]= [];
		Object.keys(this.dependencies().controls).forEach(controlName => {
			// (this.dependencies().get(controlName).get('references').value as Reference[]).filter(x=> sourcesWithDependencies().some(y => y.referenceTypeDependencies) x.type.id == this.referenceType().id &&)
			const ctrlValue = this.formService.getValue(this.dependencies().get(controlName).value);
			const foudReferences: any[] = ctrlValue?.references || [];
			if (ctrlValue?.reference) foudReferences.push(ctrlValue?.reference);
			if (foudReferences != null) {
				for (let i = 0; i < foudReferences.length; i++) {
					const foudReference = foudReferences[i];
					if (foudReference?.typeId && this.referenceDepedencyIds.includes(foudReference.typeId)) {
						const typed = foudReference as ReferencePersist;

						referenceToUse.push({
							id: typed.id,
							hash: typed.hash,
							description: typed.description,
							reference: typed.reference,
							abbreviation: typed.abbreviation,
							source: typed.source,
							sourceType: typed.sourceType,
							label: typed.label,
							definition: typed.definition,
							type: {
								id: typed.typeId,
							}
						})
					} else if (foudReference?.type?.id && this.referenceDepedencyIds.includes(foudReference.type.id)) {
						const typed = foudReference as Reference;
						if (typed != null) referenceToUse.push(typed);
					}
				}
			}
		});
		if (!isInitial && (!referenceToUse.map(x => x.reference).every(x => this.referenceToUse.map(y => y.reference).includes(x)) ||
			!this.referenceToUse.map(x => x.reference).every(x => referenceToUse.map(y => y.reference).includes(x)))) {
				this.referenceToUse = referenceToUse;
				this.form.setValue(this.multiple() ? [] : null);
				this.form.updateValueAndValidity();
		} else {
			this.referenceToUse = referenceToUse;
		}
        this.resetAutocompleteConfiguration();
	}

	onOptionSelected(selectedOption: Reference) {
		this.selectedReference.emit(selectedOption);
	}

	removeReference(reference: any) {
		if (!this.multiple()) {
			this.form.setValue(null);
			this.form.markAsDirty();
			this.form.updateValueAndValidity();
			return;
		}
		const remaining = ((this.form.value as any[]) ?? []).filter(r => r !== reference);
		this.form.setValue(remaining);
		this.form.markAsDirty();
		this.form.updateValueAndValidity();
	}

	// Names of the external sources this field searches, for a clearer hint (e.g. "ORCID, ROR").
	searchSourceNames(): string {
		return this.referenceType()?.definition?.sources?.map(s => s.label)?.filter(x => !!x)?.join(', ') ?? '';
	}

	// Unified selected-references list (single value as a one-element array) for card rendering.
	get selectedReferences(): Reference[] {
		if (this.multiple()) { return (this.form?.value as Reference[]) ?? []; }
		return this.form?.value ? [this.form.value] : [];
	}

	// Manual insertion is only possible for reference-type fields (not external-source test mode).
	get canAddManually(): boolean {
		return !this.form?.disabled && !this.sources()?.length && !!this.referenceType();
	}

	private referenceKey(r: Reference): string {
		const cfg = this.multiple() ? this.multipleAutoCompleteSearchConfiguration : this.singleAutoCompleteSearchConfiguration;
		return cfg?.uniqueAssign ? String(cfg.uniqueAssign(r)) : `${r?.source}_${r?.reference}`;
	}

	// Adds a (manually created) reference to the form value, deduping in multiple mode.
	private addReferenceToValue(reference: Reference) {
		if (this.multiple()) {
			const existing = (this.form.value as Reference[]) ?? [];
			if (!existing.some(x => this.referenceKey(x) === this.referenceKey(reference))) {
				this.form.setValue([...existing, reference]);
				this.form.markAsDirty();
				this.form.updateValueAndValidity();
			}
		} else {
			this.form.setValue(reference);
			this.form.markAsDirty();
			this.form.updateValueAndValidity();
			this.selectedReference.emit(reference);
		}
	}

	openManual(event?: Event) {
		event?.stopPropagation();
		const dialogRef = this.dialog.open(ReferenceDialogEditorComponent, {
			minWidth: 'min(720px, 95vw)',
			data: {
				referenceTypeId: this.referenceType()?.id,
				label: this.label && this.label !== '' ? this.label : this.referenceType()?.name,
			},
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((created: Reference) => {
			if (!created) { return; }
			this.addReferenceToValue(created);
		});
	}

	// Manual references are immutable once saved: editing creates a new one that replaces the old one in this field only.
	editManual(reference: Reference) {
		const dialogRef = this.dialog.open(ReferenceDialogEditorComponent, {
			minWidth: 'min(720px, 95vw)',
			data: {
				referenceTypeId: this.referenceType()?.id,
				label: this.label && this.label !== '' ? this.label : this.referenceType()?.name,
				reference: reference,
			},
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((created: Reference) => {
			if (!created) { return; }
			if (this.multiple()) {
				this.form.setValue(((this.form.value as Reference[]) ?? []).map(r => r === reference ? created : r));
			} else {
				this.form.setValue(created);
				this.selectedReference.emit(created);
			}
			this.form.markAsDirty();
			this.form.updateValueAndValidity();
		});
	}

	onPreviewReference(reference: Reference, showSelect: boolean = false) {
		if (!reference) return;
		const dialogRef = this.dialog.open(ReferenceFieldInfoDialogComponent, {
            minWidth: 'min(920px, 95vw)',
			data: {
				reference: reference,
				referenceTypeId: this.referenceType().id,
				label: this.label && this.label !== '' ? this.label : this.referenceType().name,
				showSelect: showSelect,
			},
		});
		if (showSelect) {
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(isSelected => {
				if (isSelected) {
					if (this.multiple()) {
						const existingValues = this.form.value as Reference[];
						if (existingValues == null || existingValues?.length == 0) {
							this.form.patchValue([reference]);
							this.form.markAsDirty();
						} else if (!existingValues?.some(r => JSON.stringify(r) === JSON.stringify(reference))){
							existingValues.push(reference);
							this.form.patchValue(existingValues);
							this.form.markAsDirty();
							this.form.updateValueAndValidity();
						}
					} else {
						this.form.patchValue(reference);
						this.form.markAsDirty();
						this.form.updateValueAndValidity();
					}
					
				}
			});
		}
	}

}
