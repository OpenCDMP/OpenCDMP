import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { UsageLimit, UsageLimitDefinition } from '@app/core/model/usage-limit/usage-limit';
import { UsageLimitService } from '@app/core/services/usage-limit/usage.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class UsageLimitEditorResolver extends BaseEditorResolver {

	constructor(private usageLimitService: UsageLimitService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<UsageLimit>(x => x.id),
			nameof<UsageLimit>(x => x.label),
			nameof<UsageLimit>(x => x.targetMetric),
			nameof<UsageLimit>(x => x.value),
			[nameof<UsageLimit>(x => x.definition), nameof<UsageLimitDefinition>(x => x.hasPeriodicity)].join('.'),
			[nameof<UsageLimit>(x => x.definition), nameof<UsageLimitDefinition>(x => x.periodicityRange)].join('.'),
			nameof<UsageLimit>(x => x.createdAt),
			nameof<UsageLimit>(x => x.updatedAt),
			nameof<UsageLimit>(x => x.hash),
			nameof<UsageLimit>(x => x.isActive)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...UsageLimitEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.usageLimitService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		}
	}
}
