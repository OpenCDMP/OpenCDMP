import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { WebhookSubscription } from '@app/core/model/webhook/webhook';
import { WebhookService } from '@app/core/services/webhook/webhook.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class WebhookEditorResolver extends BaseEditorResolver {

	constructor(private webhookService: WebhookService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<WebhookSubscription>(x => x.id),
			nameof<WebhookSubscription>(x => x.label),
			nameof<WebhookSubscription>(x => x.url),
			nameof<WebhookSubscription>(x => x.eventTypes),
			nameof<WebhookSubscription>(x => x.enabled),
			nameof<WebhookSubscription>(x => x.createdAt),
			nameof<WebhookSubscription>(x => x.updatedAt),
			nameof<WebhookSubscription>(x => x.hash),
			nameof<WebhookSubscription>(x => x.isActive),
			nameof<WebhookSubscription>(x => x.belongsToCurrentTenant)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...WebhookEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.webhookService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.label)), takeUntil(this._destroyed));
		}
	}
}
