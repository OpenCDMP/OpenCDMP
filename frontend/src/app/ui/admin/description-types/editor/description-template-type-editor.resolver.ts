import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class DescriptionTemplateTypeEditorResolver extends BaseEditorResolver {

	constructor(private descriptionTemplateTypeService: DescriptionTemplateTypeService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<DescriptionTemplateType>(x => x.id),
			nameof<DescriptionTemplateType>(x => x.name),
			nameof<DescriptionTemplateType>(x => x.code),
			nameof<DescriptionTemplateType>(x => x.status),
			nameof<DescriptionTemplateType>(x => x.createdAt),
			nameof<DescriptionTemplateType>(x => x.hash),
			nameof<DescriptionTemplateType>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...DescriptionTemplateTypeEditorResolver.lookupFields()
		];
		return this.descriptionTemplateTypeService.getSingle(Guid.parse(route.paramMap.get('id')), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.name)), takeUntil(this._destroyed));
	}
}
