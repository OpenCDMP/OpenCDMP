import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { VersionHistoryItem, VersionHistorySource } from '@app/ui/admin/structure-editor/version-history/version-history-source';
import { Guid } from '@common/types/guid';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { TEMPLATE_TERMS } from './template-terms';

/** Description template versions for the shared version history page. */
@Injectable()
export class TemplateVersionSource extends VersionHistorySource {
	readonly urlBase = '/asset-templates';
	readonly editPermission = AppPermission.EditDescriptionTemplate;
	readonly termOverrides = TEMPLATE_TERMS;

	constructor(private descriptionTemplateService: DescriptionTemplateService) { super(); }

	query(groupId: Guid, tenantIsSet: boolean | null): Observable<VersionHistoryItem[]> {
		const lookup = new DescriptionTemplateLookup();
		lookup.page = { offset: 0, size: 100 };
		lookup.groupIds = [groupId];
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: ['-' + nameof<DescriptionTemplate>(x => x.version)] };
		lookup.project = {
			fields: [
				nameof<DescriptionTemplate>(x => x.id), nameof<DescriptionTemplate>(x => x.label), nameof<DescriptionTemplate>(x => x.code), nameof<DescriptionTemplate>(x => x.version),
				nameof<DescriptionTemplate>(x => x.status), nameof<DescriptionTemplate>(x => x.versionStatus), nameof<DescriptionTemplate>(x => x.groupId), nameof<DescriptionTemplate>(x => x.usageCount),
				nameof<DescriptionTemplate>(x => x.createdAt), nameof<DescriptionTemplate>(x => x.updatedAt), nameof<DescriptionTemplate>(x => x.belongsToCurrentTenant)
			]
		};
		if (tenantIsSet != null) lookup.tenantIsSet = tenantIsSet;
		return this.descriptionTemplateService.query(lookup).pipe(map(result => result.items as VersionHistoryItem[]));
	}

	usageLink(version: VersionHistoryItem) { return { path: '/descriptions', queryParams: this.listingQuery('descriptionTemplateSubQuery', version) }; }

	downloadXML(id: Guid) { return this.descriptionTemplateService.downloadXML(id); }
}
