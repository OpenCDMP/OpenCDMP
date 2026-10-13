import { Component, Input, OnInit, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, UntypedFormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BaseCriteriaComponent } from '@app/ui/misc/criteria/base-criteria.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '@app/core/services/auth/auth.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { Guid } from '@common/types/guid';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferenceUsageEntityType } from '@app/core/common/enum/reference-usage-entity-type';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanBlueprintTypeService } from '@app/core/services/plan-blueprint-type/plan-blueprint-type.service';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { SingleAutoCompleteComponent } from '../../../../library/auto-complete/single/single-auto-complete.component';
import { MultipleAutoCompleteComponent } from '../../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { MatSelect, MatOption } from '@angular/material/select';
import { ReferenceUsageFilterComponent } from '../../../reference/reference-usage-filter/reference-usage-filter.component';
import { MatButton } from '@angular/material/button';

@Component({
    selector: 'app-plan-filter-component',
    templateUrl: './plan-filter.component.html',
    styleUrls: ['./plan-filter.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatSlideToggle, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, ReactiveAsteriskDirective, SingleAutoCompleteComponent, MultipleAutoCompleteComponent, MatSelect, MatOption, ReferenceUsageFilterComponent, MatButton, TranslatePipe]
})
export class PlanFilterComponent extends BaseCriteriaComponent<PlanListingFilterForm> implements OnInit {

	@Input() isPublic: boolean;
	@Input() hasSelectedTenant: boolean;
	/** Reference types shown as their own filter sections (the tenant's plan view preferences). */
	@Input() featuredReferenceTypes: ReferenceType[] = [];
	/** A reference type whose section opens straight away, e.g. when arriving from the dashboard. */
	@Input() expandReferenceTypeId: Guid = null;
	/** Hosted outside its listing (the dashboard): hides the deleted-items toggle and the status field. */
	@Input() embedded = false;

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
    roleArray = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
	ReferenceUsageEntityType = ReferenceUsageEntityType;
	selectedReferenceIds: Guid[] = [];
	filteringGrantsAsync = false;
	sizeError = false;
	maxFileSize: number = 1048576;

	descriptionTemplateAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.descriptionTemplateService.buildDescriptionTempalteGroupMultipleAutocompleteConfiguration();
	planBlueprintAutoCompleteConfiguration: MultipleAutoCompleteConfiguration = this.planBlueprintService.planBlueprintGroupMultipleAutocompleteConfiguration;
	planStatusAutoCompleteConfiguration: SingleAutoCompleteConfiguration = this.planStatusService.singleAutocompleteConfiguration;
	planBlueprintTypesAutoCompleteConfiguration : MultipleAutoCompleteConfiguration = this.planBlueprintTypeService.multipleAutocompleteConfiguration;

	constructor(
		public language: TranslateService,
		public formBuilder: UntypedFormBuilder,
		private authentication: AuthService,
		private planBlueprintTypeService: PlanBlueprintTypeService,
		private descriptionTemplateService: DescriptionTemplateService,
		private planBlueprintService: PlanBlueprintService,
		private planStatusService: PlanStatusService,
        private enumUtils: EnumUtils
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
            roles: new FormControl(filters?.roles ?? null),
			typeIds: new FormControl(filters?.typeIds ?? null),
            viewOnlyTenant: new FormControl(filters?.viewOnlyTenant ?? false),
            references: new FormControl(filters?.references ?? [])
        });
        this.selectedReferenceIds = this._flattenReferenceIds(filters?.references);
    }

    resetFilters(){
        this.formGroup.reset();
        this.formGroup.patchValue({
			typeIds: null,
            descriptionTemplates: null,
			isActive: true,
            planBlueprints: null,
            references: null,
            roles: null,
            statusId: null,
            viewOnlyTenant: null
        });
        this.selectedReferenceIds = [];
    }

	onCallbackError(error: any) {
		this.setErrorModel(error.error);
	}

	controlModified(): void {
		this.clearErrorModel();
		this.filterChanged.emit(this.formGroup.value as PlanListingFilters);
	}

	onReferencesChanged(references: ReferencesWithType[]): void {
		this.formGroup.controls.references.setValue(references ?? []);
		this.selectedReferenceIds = this._flattenReferenceIds(references);
	}

	isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}
	
	private _flattenReferenceIds(references: ReferencesWithType[]): Guid[] {
		return (references ?? []).flatMap(x => x?.referenceIds ?? []).filter(x => x != null);
	}
}

export interface PlanListingFilters {
    statusId: Guid,
	isActive: boolean,
    viewOnlyTenant: boolean,
    descriptionTemplates: Guid[],
    planBlueprints: Guid[],
    roles: PlanUserRole[],
	typeIds:  Guid[]
    references: ReferencesWithType[]
}

interface PlanListingFilterForm {
    statusId: FormControl<Guid>,
	isActive: FormControl<boolean>,
    viewOnlyTenant: FormControl<boolean>,
    descriptionTemplates: FormControl<Guid[]>,
    planBlueprints: FormControl<Guid[]>,
    roles:  FormControl<PlanUserRole[]>,
	typeIds: FormControl<Guid[]>
    references: FormControl<ReferencesWithType[]>
}