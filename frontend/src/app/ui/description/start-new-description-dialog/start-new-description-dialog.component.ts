import { Component, Inject } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { DateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { PlanDescriptionTemplateLookup } from '@app/core/query/plan-description-template.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { PlanService } from '@app/core/services/plan/plan.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AuthService } from '@app/core/services/auth/auth.service';
import { TenantLookup } from '@app/core/query/tenant.lookup';
import { Tenant } from '@app/core/model/tenant/tenant';

@Component({
	selector: 'app-start-new-description-dialog',
	templateUrl: './start-new-description-dialog.component.html',
	styleUrls: ['./start-new-description-dialog.component.scss']
})
export class StartNewDescriptionDialogComponent extends BaseComponent {

	public isDialog: boolean = false;
	public formGroup: UntypedFormGroup;
	public sections: PlanBlueprintDefinitionSection[] = [];

	planAutoCompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.planService.query(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.planService.query(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.planService.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: Plan) => item.label,
		subtitleFn: (item: Plan) => `${this.language.instant('DASHBOARD.ADD-NEW-DESCRIPTION.OPTIONS.CREATED-AT')} ${this.dateTimeFormatPipe.transform(item.createdAt, 'dd/MM/yyyy')}`,
		titleFn: (item: Plan) => item.label,
		valueAssign: (item: Plan) => item.id,
	};

	private buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[], statuses?: PlanStatusEnum[], planDescriptionTemplateSubQuery?: PlanDescriptionTemplateLookup): PlanLookup {
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
				nameof<Plan>(x => x.createdAt),
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
		public dialogRef: MatDialogRef<StartNewDescriptionDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		public dialog: MatDialog,
		private language: TranslateService,
		private planService: PlanService,
		private filterService: FilterService,
		private dateTimeFormatPipe: DateTimeFormatPipe,
		private authService: AuthService,
	) {
		super();
		this.formGroup = data.formGroup;
	}

	cancel() {
		this.dialogRef.close();
	}

	send() {
		this.dialogRef.close(this.data);
	}

	close() {
		this.dialogRef.close(false);
	}

	next() {
		this.dialogRef.close(this.data);
	}

	startNewPlan() {
		this.data.startNewPlan = true;
		this.dialogRef.close(this.data);
	}

	getDatasetDisplay(item: any): string {
		return item['label'] ? item['label'] : null;
	}
}
