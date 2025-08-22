import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormArray, FormControl, FormGroup, UntypedFormBuilder } from '@angular/forms';
import { BaseCriteriaComponent } from '@app/ui/misc/criteria/base-criteria.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { Guid } from '@common/types/guid';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { map, takeUntil } from 'rxjs';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { nameof } from 'ts-simple-nameof';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';

@Component({
    selector: 'app-plan-filter-component',
    templateUrl: './plan-filter.component.html',
    styleUrls: ['./plan-filter.component.scss'],
    standalone: false
})
export class PlanFilterComponent extends BaseCriteriaComponent<PlanListingFilterForm> implements OnInit {

	@Input() isPublic: boolean;
	@Input() hasSelectedTenant: boolean;

    private _filters: PlanListingFilters;
	@Input() set filters(v: PlanListingFilters){
        if(v){
            this._filters = v;
            this.buildForm(v);
        }
    }
    get filters(){
        return this._filters;
    }

	@Output() filterChanged: EventEmitter<PlanListingFilters> = new EventEmitter();
    

	status = PlanStatusEnum;
	role = PlanUserRole;
	filteringGrantsAsync = false;
	sizeError = false;
	maxFileSize: number = 1048576;

	descriptionTemplateAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.descriptionTemplateService.buildDescriptionTempalteGroupMultipleAutocompleteConfiguration();
	planBlueprintAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.planBlueprintService.planBlueprintGroupMultipleAutocompleteConfiguration;
	referenceTypeAutocompleteConfiguration: SingleAutoCompleteConfiguration = this.getReferenceTypeAutocompleteConfiguration();
	referenceAutocompleteConfiguration: Map<Guid, MultipleAutoCompleteConfiguration>;
	planStatusAutoCompleteConfiguration: SingleAutoCompleteConfiguration = this.planStatusService.singleAutocompleteConfiguration;

	constructor(
		public language: TranslateService,
		public formBuilder: UntypedFormBuilder,
		private authentication: AuthService,
		private descriptionTemplateService: DescriptionTemplateService,
		private planBlueprintService: PlanBlueprintService,
		private referenceTypeService: ReferenceTypeService,
		private referenceService: ReferenceService,
		private planStatusService: PlanStatusService
	) {
		super(new ValidationErrorModel());
	}
	
	ngOnInit() {
		super.ngOnInit();
	}

    buildForm(filters: PlanListingFilters){
        this.formGroup = new FormGroup<PlanListingFilterForm>({
            statusId: new FormControl(filters?.statusId),
			isActive: new FormControl(filters?.isActive),
            descriptionTemplates: new FormControl(filters?.descriptionTemplates),
            planBlueprints: new FormControl(filters?.planBlueprints),
            role: new FormControl(filters?.role),
            viewOnlyTenant: new FormControl(filters?.viewOnlyTenant ?? false),
            references: new FormArray([])
        });

        this.referenceAutocompleteConfiguration = new Map<Guid, MultipleAutoCompleteConfiguration>();

        filters?.references?.forEach((item: ReferencesWithType) => {
            if (item.referenceTypeId) {
                this.addReferenceType(item.referenceTypeId, item.referenceIds);
            } 
            else if (item.referenceIds && item.referenceIds?.length > 0) {
                this.referenceService.query(this._referenceLookup(item.referenceIds)).pipe(takeUntil(this._destroyed), map((x) => x.items))
                    .subscribe((references: Reference[]) => {
                        const types = new Set(references.map((x) => x.type.id));

                        types.forEach((typeId) => {
                            const referenceIds = references.filter((x) => x.type.id === typeId).map((x) => x.id) ?? []
                            this.addReferenceType(typeId, referenceIds);
                        })
                    });
            }
        });
    }

    resetFilters(){
        this.formGroup.reset();
        this.formGroup.patchValue({
            descriptionTemplates: null,
			isActive: true,
            planBlueprints: null,
            references: null,
            role: null,
            statusId: null,
            viewOnlyTenant: null
        });
        this.referenceAutocompleteConfiguration.clear();
    }

	onCallbackError(error: any) {
		this.setErrorModel(error.error);
	}

	controlModified(): void {
		this.clearErrorModel();
		this.filterChanged.emit(this.formGroup.value as PlanListingFilters);
	}

	addReferenceType(referenceTypeId: Guid = null, referenceIds: Guid[] = null): void {
		const referenceForm = new FormGroup<PlanListingFilterReferencesForm>({
			referenceTypeId: new FormControl(referenceTypeId),
			referenceIds: new FormControl(referenceIds ? referenceIds : []),
		});

		if (referenceTypeId && referenceIds?.length) {
            let referenceAutocomplete = this.getReferenceAutocompleteConfiguration(referenceTypeId);
            this.referenceAutocompleteConfiguration.set(referenceTypeId, referenceAutocomplete);
		}

		this._registerReferenceTypeListener(referenceForm);
		this._registerReferencesListener(referenceForm);
		
		this.formGroup.controls.references.push(referenceForm);
	}

	deleteRow(index: number): void {
		const formArray = this.formGroup.controls.references;

		formArray.removeAt(index);
	}

	isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}
	
	selectReferenceAutocompleteConfiguration(referenceTypeId: Guid): MultipleAutoCompleteConfiguration {
		return this.referenceAutocompleteConfiguration.get(referenceTypeId);
	};

	private getReferenceTypeAutocompleteConfiguration() {
		return this.referenceTypeService.getSingleAutocompleteConfiguration();
	}

	private getReferenceAutocompleteConfiguration(referenceTypeId: Guid): MultipleAutoCompleteConfiguration {
		let autocomplete = this.referenceService.getMultipleAutoCompleteQueryConfiguration([referenceTypeId]);
		return autocomplete;
	};

	private _referenceLookup(ids: Guid[]): ReferenceLookup {
		const lookup: ReferenceLookup = new ReferenceLookup();
		lookup.page = { size: 100, offset: 0 };
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<Reference>(x => x.id),
				nameof<Reference>(x => x.label),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.name)].join('.'),
			]
		};
		lookup.order = { items: [nameof<Reference>(x => x.label)] };
		return lookup;
	}

	private _registerReferenceTypeListener(formGroup: FormGroup<PlanListingFilterReferencesForm>) {
		formGroup.controls.referenceTypeId?.valueChanges.pipe(takeUntil(this._destroyed))
			.subscribe((referenceTypeId: Guid) => {
				this.referenceTypeAutocompleteConfiguration = this.getReferenceTypeAutocompleteConfiguration();
				formGroup.controls.referenceIds?.reset();

				if (referenceTypeId) {
					let referenceAutocomplete = this.getReferenceAutocompleteConfiguration(referenceTypeId);
					this.referenceAutocompleteConfiguration.set(referenceTypeId, referenceAutocomplete);
				}
			});
	}

	private _registerReferencesListener(formGroup: FormGroup<PlanListingFilterReferencesForm>) {
		formGroup.controls.referenceIds?.valueChanges.pipe(takeUntil(this._destroyed))
			.subscribe(references => {
				let referenceTypeId = formGroup.controls.referenceTypeId.value;
				if (!referenceTypeId) return;
				
				let referenceAutocomplete = this.getReferenceAutocompleteConfiguration(referenceTypeId);
				this.referenceAutocompleteConfiguration.set(referenceTypeId, referenceAutocomplete);
			});
	}
}

export interface PlanListingFilters {
    statusId: Guid,
	isActive: boolean,
    viewOnlyTenant: boolean,
    descriptionTemplates: Guid[],
    planBlueprints: Guid[],
    role: Guid,
    references: ReferencesWithType[]
}

interface PlanListingFilterForm {
    statusId: FormControl<Guid>,
	isActive: FormControl<boolean>,
    viewOnlyTenant: FormControl<boolean>,
    descriptionTemplates: FormControl<Guid[]>,
    planBlueprints: FormControl<Guid[]>,
    role:  FormControl<Guid>,
    references: FormArray<FormGroup<PlanListingFilterReferencesForm>>
}

interface PlanListingFilterReferencesForm {
    referenceTypeId: FormControl<Guid>;
    referenceIds: FormControl<Guid[]>
}