import {Component} from "@angular/core";
import {
	DescriptionTemplatesInSection,
	FieldInSection,
	PlanBlueprint,
	PlanBlueprintDefinition,
	PlanBlueprintDefinitionSection,
    ReferenceTypeFieldInSection,
    SystemFieldInSection
} from "@app/core/model/plan-blueprint/plan-blueprint";
import {PlanBlueprintService} from "@app/core/services/plan/plan-blueprint.service";
import {takeUntil} from "rxjs/operators";
import {BaseComponent} from "@common/base/base.component";
import {PlanBlueprintLookup} from "@app/core/query/plan-blueprint.lookup";
import {nameof} from "ts-simple-nameof";
import {Router} from "@angular/router";
import {RouterUtilsService} from "@app/core/services/router/router-utils.service";
import {TenantConfigurationService} from "@app/core/services/tenant-configuration/tenant-configuration.service";
import {TenantConfigurationType} from "@app/core/common/enum/tenant-configuration-type";
import {BaseEditorResolver} from "@common/base/base-editor.resolver";
import {FeaturedEntities, TenantConfiguration} from "@app/core/model/tenant-configuaration/tenant-configuration";
import {Guid} from "@common/types/guid";
import { HttpErrorHandlingService } from "@common/modules/errors/error-handling/http-error-handling.service";
import { PlanBlueprintFieldCategory } from "@app/core/common/enum/plan-blueprint-field-category";
import { EnumUtils } from "@app/core/services/utilities/enum-utils.service";
import { ReferenceType } from "@app/core/model/reference-type/reference-type";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
@Component({
	selector: 'app-top-plan-blueprints-component',
	templateUrl: './top-plan-blueprints.component.html',
	styleUrls: ['./top-plan-blueprints.component.scss'],
	standalone: false

})

export class TopPlanBlueprintsComponent extends BaseComponent{
	public lookup: PlanBlueprintLookup;
	public planBlueprints:PlanBlueprint[];
	public planBlueprintsGroupIds:Guid[];

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

    activeItem = 0;
	constructor(
        private httpErrorHandlingService: HttpErrorHandlingService, 
        private tenantConfigurationService: TenantConfigurationService,  
        protected router: Router, 
        protected enumUtils: EnumUtils
    ) {
		super();

		this.getExistingSelections()

	}

	// goTo(planBlueprint:PlanBlueprint){
	// 	this.router.navigate([this.routerUtils.generateUrl('/plans/new')], {queryParams:{blueprintId:planBlueprint.groupId}});
	// }
	public lookupFieldsFeaturedEntities(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<TenantConfiguration>(x => x.id),
			nameof<TenantConfiguration>(x => x.type),
			nameof<TenantConfiguration>(x => x.featuredEntities),

			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.id)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.label)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.description)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.groupId)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.version)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.ordinal)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints), nameof<PlanBlueprint>(x => x.ordinal)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.label)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.description)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.description)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.label)].join('.'),
            [nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<SystemFieldInSection>(x => x.systemFieldType)].join('.'),
            [nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<ReferenceTypeFieldInSection>(x => x.referenceType), nameof<ReferenceType>(x => x.name)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.fields), nameof<FieldInSection>(x => x.category)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.hasTemplates)].join('.'),
			[nameof<TenantConfiguration>(x => x.featuredEntities), nameof<FeaturedEntities>(x => x.planBlueprints),nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.sections), nameof<PlanBlueprintDefinitionSection>(x => x.descriptionTemplates), nameof<DescriptionTemplatesInSection>(x => x.descriptionTemplate), nameof<DescriptionTemplate>(x => x.label)].join('.'),


		]
	}
	getExistingSelections() {
		this.planBlueprintsGroupIds = [];
		this.tenantConfigurationService.getActiveType(TenantConfigurationType.FeaturedEntities, this.lookupFieldsFeaturedEntities())
			.pipe(takeUntil(this._destroyed)).subscribe({
			next: (data) => {
				this.planBlueprints = data?.featuredEntities?.planBlueprints || null;
			},
			error: (error) => {
                this.httpErrorHandlingService.handleBackedRequestError(error);
			}
        });
	}
}
