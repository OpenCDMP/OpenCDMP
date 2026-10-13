import { HttpResponse } from '@angular/common/http';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Guid } from '@common/types/guid';
import { Observable } from 'rxjs';
import { StructureEditorItem } from '../structure-editor-base';

export interface VersionHistoryItem extends StructureEditorItem {
	createdAt?: Date;
}

/**
 * What the version history page needs from the thing whose versions it lists. Each routing module provides one
 * on its `versions/:groupid` route.
 */
export abstract class VersionHistorySource {
	/** The route the editor lives under, e.g. `/plan-blueprints`. */
	abstract readonly urlBase: string;
	abstract readonly editPermission: AppPermission;
	/** Full i18n keys this page words differently for this kind of thing. */
	readonly termOverrides: Record<string, string> = {};
	/** Every active version of the group, newest first. `shared` scopes to the default tenant's rows. */
	abstract query(groupId: Guid, tenantIsSet: boolean | null): Observable<VersionHistoryItem[]>;
	abstract downloadXML(id: Guid): Observable<HttpResponse<Blob>>;

	/** Where the items that use one version are listed (plans for a blueprint, assets for a template), as a route and its query. */
	abstract usageLink(version: VersionHistoryItem): { path: string, queryParams: Record<string, string> };

	/** The listing query that narrows a plan or description listing to one version of its blueprint or template. */
	protected listingQuery(subQueryName: string, version: VersionHistoryItem): Record<string, string> {
		const active = [1];
		const lookup = {
			metadata: { countAll: true }, page: { offset: 0, size: 5 }, isActive: active, order: { items: ['-updatedAt'] },
			[subQueryName]: { ids: [version.id], groupIds: [version.groupId], isActive: active, metadata: { countAll: true } }
		};
		return { lookup: JSON.stringify(lookup) };
	}

	term(key: string): string { return this.termOverrides[key] ?? key; }
}
