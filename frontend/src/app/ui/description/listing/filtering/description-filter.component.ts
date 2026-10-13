
import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { ReferenceUsageEntityType } from '@app/core/common/enum/reference-usage-entity-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { TagService } from '@app/core/services/tag/tag.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseCriteriaComponent } from '@app/ui/misc/criteria/base-criteria.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { SingleAutoCompleteComponent } from '../../../../library/auto-complete/single/single-auto-complete.component';
import { MultipleAutoCompleteComponent } from '../../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { MatSelect, MatOption } from '@angular/material/select';
import { ReferenceUsageFilterComponent } from '../../../reference/reference-usage-filter/reference-usage-filter.component';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-description-filter-component',
    templateUrl: './description-filter.component.html',
    styleUrls: ['./description-filter.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatSlideToggle, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, ReactiveAsteriskDirective, SingleAutoCompleteComponent, MultipleAutoCompleteComponent, MatSelect, MatOption, ReferenceUsageFilterComponent, MatButton, TranslatePipe]
})
export class DescriptionFilterComponent extends BaseCriteriaComponent<DescriptionListingFilterForm> implements OnInit {

	@Input() isPublic: boolean;
	@Input() hasSelectedTenant: boolean;
	/** Reference types shown as their own filter sections (the tenant's description view preferences). */
	@Input() featuredReferenceTypes: ReferenceType[] = [];
	/** A reference type whose section opens straight away. */
	@Input() expandReferenceTypeId: Guid = null;
	/** Hosted outside its listing (the dashboard): hides the deleted-items toggle and the status field. */
	@Input() embedded = false;

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

	ReferenceUsageEntityType = ReferenceUsageEntityType;
	selectedReferenceIds: Guid[] = [];
	statuses = DescriptionStatusEnum;
	role = PlanUserRole;
    roleArray = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
	options: UntypedFormGroup;

	descriptionTemplateAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.descriptionTemplateService.buildDescriptionTempalteGroupMultipleAutocompleteConfiguration();
    descriptionTemplateTypeAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.descriptionTemplateTypeService.multipleAutocompleteConfiguration;
	planAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.planService.multipleAutocompleteConfiguration;
	tagAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.tagService.multipleAutocompleteConfiguration;
	descriptionStatusAutocompleteConfiguration: SingleAutoCompleteConfiguration = this.descriptionStatusService.singleAutocompleteConfiguration;

	constructor(
		public enumUtils: EnumUtils,
		private authentication: AuthService,
		private descriptionTemplateService: DescriptionTemplateService,
        private descriptionTemplateTypeService: DescriptionTemplateTypeService,
		private planService: PlanService,
		private tagService: TagService,
		private descriptionStatusService: DescriptionStatusService
	) {
		super(new ValidationErrorModel());
	}

	ngOnInit() {
		super.ngOnInit();
	}

	buildForm(filters: DescriptionListingFilters) {
		this.formGroup = new FormGroup<DescriptionListingFilterForm>({
			statusId: new FormControl(filters?.statusId),
			isActive: new FormControl(filters?.isActive),
			viewOnlyTenant: new FormControl(filters.viewOnlyTenant),
			roles: new FormControl(filters.roles),
			descriptionTemplates: new FormControl(filters.descriptionTemplates),
            descriptionTemplateTypes: new FormControl(filters.descriptionTemplateTypes),
			associatedPlanIds: new FormControl(filters.associatedPlanIds),
			tags: new FormControl(filters.tags),
			references: new FormControl(filters?.references ?? [])
		});
		this.selectedReferenceIds = this._flattenReferenceIds(filters?.references);
	}

	resetFilters() {
		this.formGroup.reset();
		this.formGroup.patchValue({
			statusId: null,
			isActive: true,
			viewOnlyTenant: null,
			roles: null,
			descriptionTemplates: null,
			associatedPlanIds: null,
			tags: null,
			references: null
		});
		this.selectedReferenceIds = [];
	}

	controlModified(): void {
		this.clearErrorModel();
		this.filterChanged.emit(this.formGroup.value as DescriptionListingFilters);
	}

	isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	onReferencesChanged(references: ReferencesWithType[]): void {
		this.formGroup.controls.references.setValue(references ?? []);
		this.selectedReferenceIds = this._flattenReferenceIds(references);
	}

	private _flattenReferenceIds(references: ReferencesWithType[]): Guid[] {
		return (references ?? []).flatMap(x => x?.referenceIds ?? []).filter(x => x != null);
	}
}

export interface DescriptionListingFilters {
	statusId: Guid,
	isActive: boolean,
	viewOnlyTenant: boolean,
	roles: PlanUserRole[],
	descriptionTemplates: Guid[],
    descriptionTemplateTypes: Guid[],
	associatedPlanIds: Guid[],
	tags: Guid[],
	references: ReferencesWithType[]
}

interface DescriptionListingFilterForm {
	statusId: FormControl<Guid>,
	isActive: FormControl<boolean>,
	viewOnlyTenant: FormControl<boolean>,
	roles: FormControl<PlanUserRole[]>,
	descriptionTemplates: FormControl<Guid[]>,
    descriptionTemplateTypes: FormControl<Guid[]>,
	associatedPlanIds: FormControl<Guid[]>,
	tags: FormControl<Guid[]>,
	references: FormControl<ReferencesWithType[]>
}
