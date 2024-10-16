
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, UntypedFormGroup } from '@angular/forms';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { ReferenceLookup } from '@app/core/query/reference.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { TagService } from '@app/core/services/tag/tag.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseCriteriaComponent } from '@app/ui/misc/criteria/base-criteria.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-description-filter-component',
	templateUrl: './description-filter.component.html',
	styleUrls: ['./description-filter.component.scss']
})
export class DescriptionFilterComponent extends BaseCriteriaComponent<DescriptionListingFilterForm> implements OnInit {

	@Input() isPublic: boolean;
	@Input() hasSelectedTenant: boolean;

	private _filters: DescriptionListingFilters;
	@Input() set filters(v: DescriptionListingFilters) {
		if (v) {
			this._filters = v;
			this.buildForm(v);
		}
	}
	get filters(): DescriptionListingFilters {
		return this._filters;
	}

	@Output() filterChanged: EventEmitter<any> = new EventEmitter();

	public criteria: any;
	public filteringTagsAsync = false;

	statuses = DescriptionStatusEnum;
	planRole = PlanUserRole;
	options: UntypedFormGroup;

	descriptionTemplateAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.descriptionTemplateService.buildMultipleAutocompleteConfiguration();
	planAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.planService.multipleAutocompleteConfiguration;
	tagAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.tagService.multipleAutocompleteConfiguration;
	referenceTypeAutocompleteConfiguration: SingleAutoCompleteConfiguration = this.getReferenceTypeAutocompleteConfiguration();
	referenceAutocompleteConfiguration: Map<Guid, MultipleAutoCompleteConfiguration>;

	constructor(
		public enumUtils: EnumUtils,
		private authentication: AuthService,
		private descriptionTemplateService: DescriptionTemplateService,
		private planService: PlanService,
		private tagService: TagService,
		private referenceService: ReferenceService,
		private referenceTypeService: ReferenceTypeService,
	) {
		super(new ValidationErrorModel());
	}

	ngOnInit() {
		super.ngOnInit();
	}

	buildForm(filters: DescriptionListingFilters) {
		this.formGroup = new FormGroup<DescriptionListingFilterForm>({
			status: new FormControl(filters?.status),
			viewOnlyTenant: new FormControl(filters.viewOnlyTenant),
			role: new FormControl(filters.role),
			descriptionTemplates: new FormControl(filters.descriptionTemplates),
			associatedPlanIds: new FormControl(filters.associatedPlanIds),
			tags: new FormControl(filters.tags),
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
						const referenceIds = references.filter((x) => x.type.id === typeId).map(x => x.id);
						this.addReferenceType(typeId, referenceIds);
					});
				});
			}
		});
	}

	resetFilters() {
		this.formGroup.reset();
		this.formGroup.patchValue({
			status: null,
			viewOnlyTenant: null,
			role: null,
			descriptionTemplates: null,
			associatedPlanIds: null,
			tags: null,
			references: null
		});
		this.referenceAutocompleteConfiguration.clear();
	}

	addReferenceType(referenceTypeId: Guid = null, referenceIds: Guid[] = null): void {
		const referenceForm = new FormGroup<DescriptionListingFilterReferencesForm>({
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

	controlModified(): void {
		this.clearErrorModel();
		this.filterChanged.emit(this.formGroup.value as DescriptionListingFilters);
	}

	isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	getReferenceTypeAutocompleteConfiguration(): SingleAutoCompleteConfiguration {
		return this.referenceTypeService.getSingleAutocompleteConfiguration();
	};

	selectReferenceAutocompleteConfiguration(referenceTypeId: Guid): MultipleAutoCompleteConfiguration {
		return this.referenceAutocompleteConfiguration.get(referenceTypeId);
	};

	getReferenceAutocompleteConfiguration(referenceTypeId: Guid): MultipleAutoCompleteConfiguration {
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

	private _registerReferenceTypeListener(formGroup: FormGroup<DescriptionListingFilterReferencesForm>) {
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

	private _registerReferencesListener(formGroup: FormGroup<DescriptionListingFilterReferencesForm>) {
		formGroup.controls.referenceIds?.valueChanges.pipe(takeUntil(this._destroyed))
			.subscribe(references => {
				let referenceTypeId = formGroup.controls.referenceTypeId.value;
				if (!referenceTypeId) return;
				
				let referenceAutocomplete = this.getReferenceAutocompleteConfiguration(referenceTypeId);
				this.referenceAutocompleteConfiguration.set(referenceTypeId, referenceAutocomplete);
			});
	}
}

export interface DescriptionListingFilters {
	status: DescriptionStatusEnum,
	viewOnlyTenant: boolean,
	role: Guid,
	descriptionTemplates: Guid[],
	associatedPlanIds: Guid[],
	tags: Guid[],
	references: ReferencesWithType[]
}

interface DescriptionListingFilterForm {
	status: FormControl<DescriptionStatusEnum>,
	viewOnlyTenant: FormControl<boolean>,
	role: FormControl<Guid>,
	descriptionTemplates: FormControl<Guid[]>,
	associatedPlanIds: FormControl<Guid[]>,
	tags: FormControl<Guid[]>,
	references: FormArray<FormGroup<DescriptionListingFilterReferencesForm>>
}

interface DescriptionListingFilterReferencesForm {
	referenceTypeId: FormControl<Guid>,
	referenceIds: FormControl<Guid[]>
}
