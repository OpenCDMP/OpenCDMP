
import { map } from 'rxjs/operators';
import { Component } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Inject } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { Plan, PlanDescriptionTemplate } from '@app/core/model/plan/plan';
import { PlanService } from '@app/core/services/plan/plan.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanDescriptionTemplateLookup } from '@app/core/query/plan-description-template.lookup';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { Guid } from '@common/types/guid';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { nameof } from 'ts-simple-nameof';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { DescriptionTemplatesInSection, PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { Tenant } from '@app/core/model/tenant/tenant';
import { AuthService } from '@app/core/services/auth/auth.service';

@Component({
	selector: 'description-copy-dialog-component',
	templateUrl: 'description-copy-dialog.component.html',
	styleUrls: ['./description-copy-dialog.component.scss'],
})
export class DescriptionCopyDialogComponent {

	sections: PlanBlueprintDefinitionSection[] = [];
	descriptionDescriptionTemplateLabel: String;
	planAutoCompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.planService.query(this.buildPlanLookup(null,null,null, this.planDescriptionTemplateLookup)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.planService.query(this.buildPlanLookup(searchQuery, null, null, this.planDescriptionTemplateLookup)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.planService.query(this.buildPlanLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: Plan) => item.label,
		titleFn: (item: Plan) => item.label,
		valueAssign: (item: Plan) => this.findSection(item),
	};

	planDescriptionTemplateLookup: PlanDescriptionTemplateLookup = {
		descriptionTemplateGroupIds: [this.data.descriptionTemplate.groupId],
		isActive: [IsActive.Active]
	} as PlanDescriptionTemplateLookup;

	private buildPlanLookup(like?: string, excludedIds?: Guid[], ids?: Guid[], planDescriptionTemplateSubQuery?: PlanDescriptionTemplateLookup): PlanLookup {
		const lookup: PlanLookup = new PlanLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.statuses = [PlanStatusEnum.Draft];
		lookup.project = {
			fields: [
				nameof<Plan>(x => x.id),
				nameof<Plan>(x => x.label),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.sectionId)].join('.'),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.descriptionTemplateGroupId)].join('.'),
				[nameof<Plan>(x => x.planDescriptionTemplates), nameof<PlanDescriptionTemplate>(x => x.isActive)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.id)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.ordinal)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
				[nameof<Plan>(x => x.blueprint), nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplateGroupId)].join('.'),

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

	constructor(
		public dialogRef: MatDialogRef<DescriptionCopyDialogComponent>,
		public planService: PlanService,
		public descriptionService: DescriptionService,
		public language: TranslateService,
		private filterService: FilterService,
		private authService: AuthService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) { }

	ngOnInit() {
	}

	findSection(plan: Plan){
		const availableSectionIds = plan.planDescriptionTemplates?.filter(x => x.descriptionTemplateGroupId === this.data.descriptionTemplate.groupId && x.isActive == IsActive.Active).map(y => y.sectionId);
		this.sections = plan.blueprint.definition.sections.filter(x => x.hasTemplates == true && availableSectionIds?.includes(x.id)) || [];
		if(this.sections.length == 1){
			this.data.formGroup.get('sectionId').setValue(this.sections[0].id);
		}else {
			this.data.formGroup.get('sectionId').setValue(null);
		}

		return plan.id
	}

	cancel() {
		this.dialogRef.close(this.data);
	}

	confirm() {
		this.dialogRef.close(this.data.formGroup);
	}

	getErrorMessage() {
		return this.language.instant('DESCRIPTION-COPY-DIALOG.ERROR-MESSAGE');
	}

	close() {
		this.dialogRef.close(false);
	}
}
