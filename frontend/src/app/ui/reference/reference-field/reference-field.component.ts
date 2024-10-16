import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
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

@Component({
	selector: 'app-reference-field-component',
	templateUrl: 'reference-field.component.html',
	styleUrls: ['./reference-field.component.scss']
})
export class ReferenceFieldComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() referenceType: ReferenceType = null;
	@Input() form: UntypedFormGroup = null;
	@Input() label: string = null;
	@Input() required: boolean = false;
	@Input() multiple: boolean = true;
	@Input() hint: string;
	@Input() placeholder: string;
	@Input() dependencies: UntypedFormGroup = null;

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
	) { super(); }

	ngOnInit() {
		this.referenceDepedencyIds = this.referenceType?.definition?.sources?.filter(x => x.referenceTypeDependencies).flatMap(x => x.referenceTypeDependencies).filter(x => x).map(x => x.id);
		if (this.referenceDepedencyIds && this.referenceDepedencyIds.length > 0) {
			this.resolveReferenceDepedency(true);
		} else {
			if (this.multiple) {
				this.multipleAutoCompleteSearchConfiguration = this.referenceService.getMultipleAutoCompleteSearchConfiguration(this.referenceType.id, null);
			} else {
				this.singleAutoCompleteSearchConfiguration = this.referenceService.getSingleAutocompleteSearchConfiguration(this.referenceType.id, null);
			}
		}
	}

	ngOnChanges(changes: SimpleChanges) {
		if ((changes['dependencies'] || changes['referenceType']) && this.dependencies != null && this.referenceType != null) {
			this.referenceDepedencyIds = this.referenceType?.definition?.sources?.filter(x => x.referenceTypeDependencies).flatMap(x => x.referenceTypeDependencies).filter(x => x).map(x => x.id);
			if (this.referenceDepedencyIds && this.referenceDepedencyIds.length > 0) {
				if (this.dependenciesSubscription != null) this.dependenciesSubscription.unsubscribe();
				this.resolveReferenceDepedency(true);
				this.dependenciesSubscription = this.dependencies.valueChanges.pipe(takeUntil(this._destroyed)).subscribe(changes => {
							this.resolveReferenceDepedency(false);
						});
				}
		}
	}

	resolveReferenceDepedency(isInitial: boolean) {
		const referenceToUse : Reference[]= [];
		Object.keys(this.dependencies.controls).forEach(controlName => {
			// (this.dependencies.get(controlName).get('references').value as Reference[]).filter(x=> sourcesWithDependencies.some(y => y.referenceTypeDependencies) x.type.id == this.referenceType.id &&)
			const ctrlValue = this.formService.getValue(this.dependencies.get(controlName).value);
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
				this.form.setValue(this.multiple ? [] : null);
				this.form.updateValueAndValidity();
		} else {
			this.referenceToUse = referenceToUse;
		}

		if (this.multiple) {
			this.multipleAutoCompleteSearchConfiguration = this.referenceService.getMultipleAutoCompleteSearchConfiguration(this.referenceType.id, this.referenceToUse);
		} else {
			this.singleAutoCompleteSearchConfiguration = this.referenceService.getSingleAutocompleteSearchConfiguration(this.referenceType.id, this.referenceToUse);
		}
	}

	addReference() {
		const dialogRef = this.dialog.open(ReferenceDialogEditorComponent, {
			minWidth: '49%',
			restoreFocus: false,
			data: {
				referenceTypeId: this.referenceType.id,
				label: this.label && this.label !== '' ? this.label : this.referenceType.name,
				},
			});
			dialogRef.afterClosed()
				.pipe(takeUntil(this._destroyed))
				.subscribe(newResult => {
					if (!newResult) { return; }
					if (this.multiple) {
						let results = this.form.value as ReferencePersist[];
						if (results == undefined) results = [];
						results.push(newResult);
						this.form.setValue(results);
					} else {
						this.form.setValue(newResult);
					}
				});
	}
}
