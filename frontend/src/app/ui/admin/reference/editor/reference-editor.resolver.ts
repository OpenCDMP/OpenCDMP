import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Definition, Field, Reference } from '@app/core/model/reference/reference';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class ReferenceEditorResolver extends BaseEditorResolver {

	constructor(private referenceService: ReferenceService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<Reference>(x => x.id),
			nameof<Reference>(x => x.label),
			[nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
			nameof<Reference>(x => x.description),
			nameof<Reference>(x => x.reference),
			nameof<Reference>(x => x.abbreviation),
			nameof<Reference>(x => x.source),
			nameof<Reference>(x => x.sourceType),
			nameof<Reference>(x => x.createdAt),
			nameof<Reference>(x => x.updatedAt),
			[nameof<Reference>(x => x.definition), nameof<Definition>(x => x.fields), nameof<Field>(x => x.code)].join('.'),
			[nameof<Reference>(x => x.definition), nameof<Definition>(x => x.fields), nameof<Field>(x => x.dataType)].join('.'),
			[nameof<Reference>(x => x.definition), nameof<Definition>(x => x.fields), nameof<Field>(x => x.value)].join('.'),
			nameof<Reference>(x => x.hash),
			nameof<Reference>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...ReferenceEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.referenceService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		}
	}
}
