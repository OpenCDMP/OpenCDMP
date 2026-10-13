import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanBlueprintTypeStatus } from '@app/core/common/enum/plan-blueprint-type-status';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintType } from '@app/core/model/plan-blueprint-type/plan-blueprint-type';
import { DescriptionTemplateTypeLookup } from '@app/core/query/description-template-type.lookup';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { PlanBlueprintTypeLookup } from '@app/core/query/plan-blueprint-type.lookup';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanBlueprintTypeService } from '@app/core/services/plan-blueprint-type/plan-blueprint-type.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Guid } from '@common/types/guid';
import { forkJoin, Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { TypeItem, TypeSectionComponent, TypeSectionConfig } from './type-section.component';

/** The categories that group plan blueprints and asset templates, managed in one place by the people who curate them. */
@Component({
	selector: 'app-types-page',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, TypeSectionComponent],
	templateUrl: './types-page.component.html',
	styleUrl: './types-page.component.scss',
})
export class TypesPageComponent {
	private readonly authService = inject(AuthService);
	private readonly planBlueprintTypeService = inject(PlanBlueprintTypeService);
	private readonly planBlueprintService = inject(PlanBlueprintService);
	private readonly descriptionTemplateTypeService = inject(DescriptionTemplateTypeService);
	private readonly descriptionTemplateService = inject(DescriptionTemplateService);
	private readonly routerUtils = inject(RouterUtilsService);
	private readonly queryParamsService = inject(QueryParamsService);

	protected readonly blueprintTypes: TypeSectionConfig | null = this.authService.hasPermission(AppPermission.ViewPlanBlueprintTypePage) ? {
		titleKey: 'TYPES-PAGE.BLUEPRINT-TYPES.TITLE',
		descriptionKey: 'TYPES-PAGE.BLUEPRINT-TYPES.DESCRIPTION',
		usesKey: 'TYPES-PAGE.BLUEPRINT-TYPES.USES',
		reorderable: true,
		canEdit: this.authService.hasPermission(AppPermission.EditPlanBlueprintType),
		canDelete: this.authService.hasPermission(AppPermission.DeletePlanBlueprintType),
		load: () => this.planBlueprintTypeService.query(this.blueprintTypeLookup()).pipe(
			switchMap(result => result.items.length === 0 ? of([] as TypeItem[]) : forkJoin(result.items.map(type => this.countBlueprints(type.id).pipe(map(uses => ({
				id: type.id,
				hash: type.hash,
				name: type.name,
				code: type.code,
				ordinal: type.ordinal,
				available: type.status === PlanBlueprintTypeStatus.Finalized,
				shared: type.belongsToCurrentTenant === false,
				uses
			}))))))
		),
		save: item => this.planBlueprintTypeService.persist({
			id: item.id,
			hash: item.hash,
			name: item.name,
			code: item.code,
			ordinal: item.ordinal,
			status: item.available ? PlanBlueprintTypeStatus.Finalized : PlanBlueprintTypeStatus.Draft
		}),
		delete: id => this.planBlueprintTypeService.delete(id),
		listLink: id => ({
			route: this.routerUtils.generateUrl('/plan-blueprints'),
			lookup: this.queryParamsService.serializeLookup({ typeIds: [id] } as PlanBlueprintLookup)
		})
	} : null;

	protected readonly templateTypes: TypeSectionConfig | null = this.authService.hasPermission(AppPermission.ViewDescriptionTemplateTypePage) ? {
		titleKey: 'TYPES-PAGE.TEMPLATE-TYPES.TITLE',
		descriptionKey: 'TYPES-PAGE.TEMPLATE-TYPES.DESCRIPTION',
		usesKey: 'TYPES-PAGE.TEMPLATE-TYPES.USES',
		reorderable: false,
		canEdit: this.authService.hasPermission(AppPermission.EditDescriptionTemplateType),
		canDelete: this.authService.hasPermission(AppPermission.DeleteDescriptionTemplateType),
		load: () => this.descriptionTemplateTypeService.query(this.templateTypeLookup()).pipe(
			switchMap(result => result.items.length === 0 ? of([] as TypeItem[]) : forkJoin(result.items.map(type => this.countTemplates(type.id).pipe(map(uses => ({
				id: type.id,
				hash: type.hash,
				name: type.name,
				code: type.code,
				available: type.status === DescriptionTemplateTypeStatus.Finalized,
				shared: type.belongsToCurrentTenant === false,
				uses
			}))))))
		),
		save: item => this.descriptionTemplateTypeService.persist({
			id: item.id,
			hash: item.hash,
			name: item.name,
			code: item.code,
			status: item.available ? DescriptionTemplateTypeStatus.Finalized : DescriptionTemplateTypeStatus.Draft
		}),
		delete: id => this.descriptionTemplateTypeService.delete(id),
		listLink: id => ({
			route: this.routerUtils.generateUrl('/asset-templates'),
			lookup: this.queryParamsService.serializeLookup({ typeIds: [id] } as DescriptionTemplateLookup)
		})
	} : null;

	private blueprintTypeLookup(): PlanBlueprintTypeLookup {
		const lookup = new PlanBlueprintTypeLookup();
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 200 };
		lookup.order = { items: [nameof<PlanBlueprintType>(x => x.ordinal)] };
		lookup.project = {
			fields: [
				nameof<PlanBlueprintType>(x => x.id),
				nameof<PlanBlueprintType>(x => x.name),
				nameof<PlanBlueprintType>(x => x.code),
				nameof<PlanBlueprintType>(x => x.ordinal),
				nameof<PlanBlueprintType>(x => x.status),
				nameof<PlanBlueprintType>(x => x.hash),
				nameof<PlanBlueprintType>(x => x.belongsToCurrentTenant)
			]
		};
		return lookup;
	}

	private templateTypeLookup(): DescriptionTemplateTypeLookup {
		const lookup = new DescriptionTemplateTypeLookup();
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 200 };
		lookup.order = { items: [nameof<DescriptionTemplateType>(x => x.name)] };
		lookup.project = {
			fields: [
				nameof<DescriptionTemplateType>(x => x.id),
				nameof<DescriptionTemplateType>(x => x.name),
				nameof<DescriptionTemplateType>(x => x.code),
				nameof<DescriptionTemplateType>(x => x.status),
				nameof<DescriptionTemplateType>(x => x.hash),
				nameof<DescriptionTemplateType>(x => x.belongsToCurrentTenant)
			]
		};
		return lookup;
	}

	private countBlueprints(typeId: Guid): Observable<number> {
		const lookup = new PlanBlueprintLookup();
		lookup.typeIds = [typeId];
		lookup.isActive = [IsActive.Active];
		lookup.versionStatuses = [PlanBlueprintVersionStatus.Current, PlanBlueprintVersionStatus.NotFinalized];
		lookup.page = { offset: 0, size: 1 };
		lookup.order = { items: [nameof<PlanBlueprint>(x => x.id)] };
		lookup.metadata = { countAll: true };
		lookup.project = { fields: [nameof<PlanBlueprint>(x => x.id)] };
		return this.planBlueprintService.query(lookup).pipe(map(result => result.count));
	}

	private countTemplates(typeId: Guid): Observable<number> {
		const lookup = new DescriptionTemplateLookup();
		lookup.typeIds = [typeId];
		lookup.isActive = [IsActive.Active];
		lookup.versionStatuses = [DescriptionTemplateVersionStatus.Current, DescriptionTemplateVersionStatus.NotFinalized];
		lookup.page = { offset: 0, size: 1 };
		lookup.order = { items: [nameof<DescriptionTemplate>(x => x.id)] };
		lookup.metadata = { countAll: true };
		lookup.project = { fields: [nameof<DescriptionTemplate>(x => x.id)] };
		return this.descriptionTemplateService.query(lookup).pipe(map(result => result.count));
	}
}
