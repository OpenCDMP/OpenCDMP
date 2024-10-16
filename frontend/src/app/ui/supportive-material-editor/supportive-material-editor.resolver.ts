import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SupportiveMaterial } from '@app/core/model/supportive-material/supportive-material';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class SupportiveMaterialEditorResolver extends BaseEditorResolver {

	constructor(private supportiveMaterialService: SupportiveMaterialService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<SupportiveMaterial>(x => x.id),
			nameof<SupportiveMaterial>(x => x.type),
			nameof<SupportiveMaterial>(x => x.languageCode),
			nameof<SupportiveMaterial>(x => x.payload),
			nameof<SupportiveMaterial>(x => x.createdAt),
			nameof<SupportiveMaterial>(x => x.updatedAt),
			nameof<SupportiveMaterial>(x => x.hash),
			nameof<SupportiveMaterial>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...SupportiveMaterialEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.supportiveMaterialService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.languageCode)), takeUntil(this._destroyed));
		}
	}
}
