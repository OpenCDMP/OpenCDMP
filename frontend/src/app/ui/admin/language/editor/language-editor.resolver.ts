import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Language } from '@app/core/model/language/language';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class LanguageEditorResolver extends BaseEditorResolver {

	constructor(private languageV2Service: LanguageHttpService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Language>(x => x.id),
			nameof<Language>(x => x.code),
			nameof<Language>(x => x.payload),
			nameof<Language>(x => x.ordinal),
			nameof<Language>(x => x.createdAt),
			nameof<Language>(x => x.updatedAt),
			nameof<Language>(x => x.hash),
			nameof<Language>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...LanguageEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.languageV2Service.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.code)), takeUntil(this._destroyed));
		}
	}
}
