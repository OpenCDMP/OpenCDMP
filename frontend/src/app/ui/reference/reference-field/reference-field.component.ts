import { Component, effect, EventEmitter, input, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';
import { ReferenceDialogEditorComponent } from './editor/reference-dialog-editor.component';
import { Reference, ReferencePersist } from '@app/core/model/reference/reference';
import { Guid } from '@common/types/guid';
import { Subscription } from 'rxjs';
import { FormService } from '@common/forms/form-service';
import { ExternalFetcherBaseSourceConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';

@Component({
    selector: 'app-reference-field-component',
    templateUrl: 'reference-field.component.html',
    styleUrls: ['./reference-field.component.scss'],
    standalone: false
})
export class ReferenceFieldComponent extends BaseComponent {
    static nextId = 0

	sources = input<ExternalFetcherBaseSourceConfigurationPersist []>();
	dependencies = input<UntypedFormGroup>();
	referenceType = input<ReferenceType>();
	multiple = input<boolean>(true);
	definitionSourcekey = input<string>();

	@Input() form: UntypedFormGroup = null;
	@Input() label: string = null;
	@Input() required: boolean = false;
	@Input() hint: string;
	@Input() placeholder: string;
    @Input() id: string = `multiple-autocomplete-${ReferenceFieldComponent.nextId++}`;
	@Output() selectedReference: EventEmitter<any> = new EventEmitter();

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
            const multiple = this.multiple();
            const definitionSourcekey = this.definitionSourcekey();

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
                this.referenceService.getMultipleAutoCompleteTestSearchConfiguration(this.referenceToUse, this.definitionSourcekey(), this.sources()) :
                this.referenceService.getMultipleAutoCompleteSearchConfiguration(this.referenceType().id, this.referenceToUse);
        } else {
            this.multipleAutoCompleteSearchConfiguration = null;
            this.singleAutoCompleteSearchConfiguration = this.sources()?.length ? 
                this.referenceService.getSingleAutocompleteTestSearchConfiguration(this.referenceToUse, this.definitionSourcekey(), this.sources()) :
                this.referenceService.getSingleAutocompleteSearchConfiguration(this.referenceType().id, this.referenceToUse);
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

	addReference() {
		const dialogRef = this.dialog.open(ReferenceDialogEditorComponent, {
			restoreFocus: false,
            minWidth: 'min(920px, 95vw)',
			data: {
				referenceTypeId: this.referenceType().id,
				label: this.label && this.label !== '' ? this.label : this.referenceType().name,
				},
			});
			dialogRef.afterClosed()
				.pipe(takeUntil(this._destroyed))
				.subscribe(newResult => {
					if (!newResult) { return; }
					if (this.multiple()) {
						let results = this.form.value as ReferencePersist[];
						if (results == undefined) results = [];
						results.push(newResult);
						this.form.setValue(results);
                        this.form.updateValueAndValidity();
					} else {
						this.form.setValue(newResult);
                        this.form.updateValueAndValidity();
					}
				});
	}

	onOptionSelected(selectedOption: Reference) {
		this.selectedReference.emit(selectedOption);
	}

}
