
import { Component, Inject } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { Description } from "@app/core/model/description/description";
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan, PlanDescriptionTemplate } from '@app/core/model/plan/plan';
import { Tenant } from '@app/core/model/tenant/tenant';
import { PlanDescriptionTemplateLookup } from '@app/core/query/plan-description-template.lookup';
import { PlanStatusLookup } from '@app/core/query/plan-status.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from "@ngx-translate/core";
import { map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";

@Component({
    selector: 'description-copy-dialog-component',
    templateUrl: 'description-copy-dialog.component.html',
    styleUrls: ['./description-copy-dialog.component.scss'],
    standalone: false
})
export class DescriptionCopyDialogComponent {

	sections: PlanBlueprintDefinitionSection[] = [];
	descriptionDescriptionTemplateLabel: String;
	planAutoCompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.planService.query(this.buildPlanLookup(null, null, null, this.planDescriptionTemplateLookup)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.planService.query(this.buildPlanLookup(searchQuery, null, null, this.planDescriptionTemplateLookup)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.planService.query(this.buildPlanLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: Plan) => item.label,
		titleFn: (item: Plan) => item.label,
		valueAssign: (item: Plan) => this.findSection(item),
	};

	planDescriptionTemplateLookup: PlanDescriptionTemplateLookup;

	private buildPlanLookup(like?: string, excludedIds?: Guid[], ids?: Guid[], planDescriptionTemplateSubQuery?: PlanDescriptionTemplateLookup): PlanLookup {
		const lookup: PlanLookup = new PlanLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		const planStatusLookup: PlanStatusLookup = new PlanStatusLookup();
		planStatusLookup.internalStatuses = [PlanStatusEnum.Draft];
		lookup.planStatusSubQuery = planStatusLookup;
		lookup.project = {
			fields: [
				nameof<Plan>(x => x.id),
				nameof<Plan>(x => x.label),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.id)].join('.'),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.ordinal)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.groupId)].join('.'),

			]
		};

		if (this.authService.selectedTenant() != 'default') {
			lookup.tenantSubQuery = new TenantLookup();
			lookup.tenantSubQuery.metadata = { countAll: true };
			lookup.tenantSubQuery.isActive = [IsActive.Active];
			lookup.tenantSubQuery.project = {
				fields: [
					nameof<Tenant>(x => x.code),
				]
			};
			lookup.tenantSubQuery.codes = [this.authService.selectedTenant()];
		}

		if (planDescriptionTemplateSubQuery != null) lookup.planDescriptionTemplateSubQuery = planDescriptionTemplateSubQuery;
		lookup.order = { items: [nameof<Plan>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

    formGroup = new FormGroup({
        planId: new FormControl<Guid>(null, Validators.required),
        sectionId: new FormControl<Guid>(null, Validators.required),
    })
    description: Description;
    planDescriptionTemplates: PlanDescriptionTemplate[] = null;

	constructor(
		public dialogRef: MatDialogRef<DescriptionCopyDialogComponent>,
		public planService: PlanService,
		public descriptionService: DescriptionService,
		public language: TranslateService,
		private filterService: FilterService,
		private authService: AuthService,
		@Inject(MAT_DIALOG_DATA) public data: CopyDialogInputParams
	) { 
        this.description = data.description;
        this.planDescriptionTemplateLookup = {
            descriptionTemplateGroupIds: [this.description.descriptionTemplate.groupId],
            isActive: [IsActive.Active]
        } as PlanDescriptionTemplateLookup;
    }

	ngOnInit() {
	}

	findSection(plan: Plan) {
		const availableSectionIds = plan.planDescriptionTemplates?.filter(x => x.descriptionTemplateGroupId === this.description.descriptionTemplate.groupId && x.isActive == IsActive.Active).map(y => y.sectionId);
		this.sections = plan.blueprint.definition.sections.filter(x => x.hasTemplates == true && availableSectionIds?.includes(x.id)) || [];
		if (this.sections.length == 1) {
            this.formGroup.controls.sectionId.setValue(this.sections[0].id);
		} else {
			this.formGroup.get('sectionId').setValue(null);
		}
        this.planDescriptionTemplates = plan.planDescriptionTemplates;
		return plan.id
	}

	cancel() {
		this.dialogRef.close();
	}

	confirm() {
        if(!this.formGroup.valid){ 
            this.formGroup.markAllAsTouched();
            return;
        }
		this.dialogRef.close(this.formGroup.value);
	}

	getErrorMessage() {
		return this.language.instant('DESCRIPTION-COPY-DIALOG.ERROR-MESSAGE');
	}

	close() {
		this.dialogRef.close();
	}
}

export interface CopyDialogInputParams {
    formGroup: FormGroup<any>;
    description: Description;
    descriptionTemplate: DescriptionTemplate;
    planDescriptionTemplate: PlanDescriptionTemplate;
    descriptionProfileExist: boolean;
    confirmButton: string;
    cancelButton: string;
}
export interface CopyDialogReturnParams {
    planId: Guid;
    sectionId: Guid;
}