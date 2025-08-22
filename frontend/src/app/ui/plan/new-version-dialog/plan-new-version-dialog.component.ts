import { Component, Inject } from '@angular/core';
import { AbstractControl, FormArray, UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Plan, NewVersionPlanDescriptionPersist, NewVersionPlanPersist } from '@app/core/model/plan/plan';
import { PlanService } from '@app/core/services/plan/plan.service';
import { SnackBarNotificationLevel } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { catchError, map, takeUntil, tap } from 'rxjs/operators';
import { PlanNewVersionDialogEditorModel } from './plan-new-version-dialog.editor.model';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanEditorEntityResolver } from '../plan-editor-blueprint/resolvers/plan-editor-enitity.resolver';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { FormService } from '@common/forms/form-service';
import { MatSelectionListChange } from '@angular/material/list';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Description } from '@app/core/model/description/description';
import { of, throwError } from 'rxjs';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { ResponseErrorCode } from '@app/core/common/enum/respone-error-code';

@Component({
    selector: 'app-plan-new-version-dialog',
    templateUrl: './plan-new-version-dialog.component.html',
    styleUrls: ['./plan-new-version-dialog.component.scss'],
    standalone: false
})
export class NewVersionPlanDialogComponent extends BaseComponent {
   

	plan: Plan;
	editorModel: PlanNewVersionDialogEditorModel;
	formGroup: UntypedFormGroup;
	selectedBlueprintSections: PlanBlueprintDefinitionSection[];

	singleAutocompleteBlueprintConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.planBlueprintService.query(this.buildAutocompleteLookup(null, null, null, [PlanBlueprintStatus.Finalized])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.planBlueprintService.query(this.buildAutocompleteLookup(searchQuery, null, null, [PlanBlueprintStatus.Finalized])).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.planBlueprintService.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: PlanBlueprint) => item.label,
		subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' '+ item.version,
		titleFn: (item: PlanBlueprint) => item.label,
		valueAssign: (item: PlanBlueprint) => item.id,
	};

	public buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[], statuses?: PlanBlueprintStatus[]): PlanBlueprintLookup {
		const lookup: PlanBlueprintLookup = new PlanBlueprintLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.statuses = statuses;
		lookup.project = {
			fields: [
				nameof<PlanBlueprint>(x => x.id),
				nameof<PlanBlueprint>(x => x.label),
				nameof<PlanBlueprint>(x => x.version),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
				[nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),
			]
		};
		lookup.order = { items: [nameof<PlanBlueprint>(x => x.label)] };
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Previous, PlanBlueprintVersionStatus.Current];
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

    IsActiveEnum = IsActive;
	descriptionStatusEnum = DescriptionStatusEnum;
	constructor(
		public dialogRef: MatDialogRef<NewVersionPlanDialogComponent>,
		private planService: PlanService,
		public planBlueprintService: PlanBlueprintService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private language: TranslateService,
		private filterService: FilterService,
		private formService: FormService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		super();
		this.plan = data.plan;
		this.plan.planDescriptionTemplates = this.plan.planDescriptionTemplates?.filter(x => x.isActive === IsActive.Active);
	}

	get allDescriptionsNo(): number{
		const allDescriptionIds = (this.formGroup.get('descriptions').value as NewVersionPlanDescriptionPersist[]).map( x => x.descriptionId);

		return allDescriptionIds?.length ?? 0;
	}

	get checkedDescrionsNo(): number {
		const selectedDescriptionIds = (this.formGroup.get('descriptions').value as NewVersionPlanDescriptionPersist[]).filter(y => y.blueprintSectionId != undefined).map( x => x.descriptionId);

		return selectedDescriptionIds?.length ?? 0;
	}

	get allDescriptionsCompleted(): boolean {
		return this.allDescriptionsNo === this.checkedDescrionsNo;
	}

	get someDescriptionsCompleted(): boolean {
		return this.checkedDescrionsNo > 0 && this.checkedDescrionsNo < this.allDescriptionsNo;
	}

	get isStartinBlueprintInactive(): boolean{
		return this.plan.blueprint?.isActive === IsActive.Inactive;
	}


	ngOnInit() {
		this.selectedBlueprintSections = this.plan.blueprint?.definition?.sections?.filter(x => x.hasTemplates) || null;
		this.editorModel = new PlanNewVersionDialogEditorModel().fromModel(this.plan, this.plan.blueprint, null, null, this.isStartinBlueprintInactive);
		this.formGroup = this.editorModel.buildForm();
	}

	selectedBlueprintChanged(item: PlanBlueprint): void{
		this.selectedBlueprintSections = item.definition?.sections?.filter(x => x.hasTemplates) || null;
		if(this.selectedBlueprintSections && this.hasDescriptions()) {
			this.formGroup = this.editorModel.fromModel(this.plan, item, this.formGroup.get('label').value, this.formGroup.get('description').value).buildForm();
		}
	}

	descriptionSelectionChanged(event: MatSelectionListChange) {
		const descriptionsFormArray = this.formGroup.get('descriptions') as FormArray;
		this.patchSelectedDescriptions(descriptionsFormArray, event.source._value);
	}

	patchSelectedDescriptions(descriptionsFormArray: FormArray, selectedDescriptionIds: string[]): void {
		const descriptionIds = (this.formGroup.get('descriptions').value as NewVersionPlanDescriptionPersist[]).filter(y => y.blueprintSectionId != undefined).map( x => x.descriptionId);

		if (selectedDescriptionIds.length > 0 && descriptionIds.length > 0){		
			if (selectedDescriptionIds.length < descriptionIds.length){
				const mustDeleteDescription = descriptionIds.filter( id => selectedDescriptionIds.indexOf(id.toString()) < 0) || []
				if (mustDeleteDescription.length > 0){
					mustDeleteDescription.forEach(x => this.removeSelectedDescription(descriptionsFormArray, x));
				}
			}
		} else {
			if (descriptionIds?.length > 0){
				descriptionIds.forEach(x => this.removeSelectedDescription(descriptionsFormArray, x))
			}
		}

		descriptionsFormArray.controls.forEach((control) => {

			if (this._isDescriptionSelected(control, selectedDescriptionIds)
				&& (!this._isDescriptionInSection(control))) {
					this._patchDefaultSection(control);
			}
		});
	}

	removeSelectedDescription(descriptionFormArray: FormArray, descriptionId: Guid){
		this.getDescriptionSectionFormControl(descriptionFormArray, descriptionId).patchValue(null);
	}

	getDescriptionSectionLabel(descriptionFormArray: FormArray, descriptionId: Guid) {
		const descriptionSection = this.getDescriptionSectionFormControl(descriptionFormArray, descriptionId);
		return this.selectedBlueprintSections.find(s => s.id == descriptionSection?.value)?.label;
	}

	getDescriptionSectionFormControl(descriptionFormArray: FormArray, descriptionId: Guid) {
		const index = descriptionFormArray.controls.findIndex(x => x.get('descriptionId')?.value == descriptionId);
		return descriptionFormArray.at(index).get('blueprintSectionId');
	}

	isDefaultSelected(descriptionFormArray: FormArray, descriptionId: Guid): boolean{
		return this.getDescriptionSectionFormControl(descriptionFormArray, descriptionId).value != null;
	}

	hasDescriptions() {
		return this.plan.descriptions?.length > 0;
	}

	close() {
		this.dialogRef.close(null);
	}

	cancel() {
		this.dialogRef.close(null);
	}

	confirm = () => {
		if (!this.formGroup.valid) { return; }
		const formData = this.formService.getValue(this.formGroup.value) as NewVersionPlanPersist;
		if (formData.descriptions.length > 0){
			formData.descriptions = formData.descriptions.filter(x => x.blueprintSectionId != null)
		}
        return this.planService.newVersion(formData, PlanEditorEntityResolver.lookupFields())
        .pipe(
            takeUntil(this._destroyed),
            catchError((error) => {this.onCallbackError(error); return of(null)}),
            tap((plan) => {
                if (plan) this.dialogRef.close(plan);
            })
        );
	}

	toggleAllDescriptions(event: any) {
		const descriptionFormArray = this.formGroup.get('descriptions') as FormArray;

		if (event === true) {
			descriptionFormArray.controls.forEach(control => {
				this._patchDefaultSection(control)
			});
		} else {
			descriptionFormArray.controls.forEach(control => {
				control.get('blueprintSectionId')?.patchValue(null);
			});
		}
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		let errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, errorResponse.error.message ? errorResponse.error.message : errorResponse.error.error ? errorResponse.error.error : this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-DELETE'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);
	}

	selectTemplate(event: any): void {
		event?.stopPropagation();
	}

	private _patchDefaultSection(control: AbstractControl): void {
		if (this.selectedBlueprintSections?.length > 0) {
			control.get('blueprintSectionId').patchValue(this.selectedBlueprintSections[0].id);
		}
	}

	private _isDescriptionSelected(control: AbstractControl, selectedDescriptionIds: string[]): boolean {
		return selectedDescriptionIds.includes(control.get('descriptionId')?.value)	
	}

	private _isDescriptionInSection(control: AbstractControl): boolean {
		return control.get('blueprintSectionId') != null && control.get('blueprintSectionId')?.value != null;
	}
}
