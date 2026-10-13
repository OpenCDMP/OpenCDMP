import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { VersionHistoryItem, VersionHistorySource } from '@app/ui/admin/structure-editor/version-history/version-history-source';
import { Guid } from '@common/types/guid';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

/** Plan blueprint versions for the shared version history page. */
@Injectable()
export class BlueprintVersionSource extends VersionHistorySource {
	readonly urlBase = '/plan-blueprints';
	readonly editPermission = AppPermission.EditPlanBlueprint;

	constructor(private planBlueprintService: PlanBlueprintService) { super(); }

	query(groupId: Guid, tenantIsSet: boolean | null): Observable<VersionHistoryItem[]> {
		const lookup = new PlanBlueprintLookup();
		lookup.page = { offset: 0, size: 100 };
		lookup.groupIds = [groupId];
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: ['-' + nameof<PlanBlueprint>(x => x.version)] };
		lookup.project = {
			fields: [
				nameof<PlanBlueprint>(x => x.id), nameof<PlanBlueprint>(x => x.label), nameof<PlanBlueprint>(x => x.code), nameof<PlanBlueprint>(x => x.version),
				nameof<PlanBlueprint>(x => x.status), nameof<PlanBlueprint>(x => x.versionStatus), nameof<PlanBlueprint>(x => x.groupId), nameof<PlanBlueprint>(x => x.usageCount),
				nameof<PlanBlueprint>(x => x.createdAt), nameof<PlanBlueprint>(x => x.updatedAt), nameof<PlanBlueprint>(x => x.belongsToCurrentTenant)
			]
		};
		if (tenantIsSet != null) lookup.tenantIsSet = tenantIsSet;
		return this.planBlueprintService.query(lookup).pipe(map(result => result.items as VersionHistoryItem[]));
	}

	usageLink(version: VersionHistoryItem) { return { path: '/plans', queryParams: this.listingQuery('planBlueprintSubQuery', version) }; }

	downloadXML(id: Guid) { return this.planBlueprintService.downloadXML(id); }
}
