import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Language } from '@app/core/model/language/language';
import { NotificationFieldInfo, NotificationFieldOptions, NotificationTemplate, NotificationTemplateValue } from '@notification-service/core/model/notification-template.model';
import { NotificationTemplateService } from '@notification-service/services/http/notification-template.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditorResolver } from '@common/base/base-editor.resolver';
import { Guid } from '@common/types/guid';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class NotificationTemplateEditorResolver extends BaseEditorResolver {

	constructor(private notificationTemplateService: NotificationTemplateService, private breadcrumbService: BreadcrumbService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<NotificationTemplate>(x => x.id),
			nameof<NotificationTemplate>(x => x.channel),
			nameof<NotificationTemplate>(x => x.notificationType),
			nameof<NotificationTemplate>(x => x.kind),

			nameof<NotificationTemplate>(x => x.languageCode),

			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectText)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectKey)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectFieldOptions), nameof<NotificationFieldOptions>(x => x.mandatory)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectFieldOptions), nameof<NotificationFieldOptions>(x => x.optional)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectFieldOptions), nameof<NotificationFieldOptions>(x => x.optional), nameof<NotificationFieldInfo>(x => x.key)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectFieldOptions), nameof<NotificationFieldOptions>(x => x.optional), nameof<NotificationFieldInfo>(x => x.type)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectFieldOptions), nameof<NotificationFieldOptions>(x => x.optional), nameof<NotificationFieldInfo>(x => x.value)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.subjectFieldOptions), nameof<NotificationFieldOptions>(x => x.formatting)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyText)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyKey)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyFieldOptions), nameof<NotificationFieldOptions>(x => x.mandatory)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyFieldOptions), nameof<NotificationFieldOptions>(x => x.optional)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyFieldOptions), nameof<NotificationFieldOptions>(x => x.optional), nameof<NotificationFieldInfo>(x => x.key)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyFieldOptions), nameof<NotificationFieldOptions>(x => x.optional), nameof<NotificationFieldInfo>(x => x.type)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyFieldOptions), nameof<NotificationFieldOptions>(x => x.optional), nameof<NotificationFieldInfo>(x => x.value)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bodyFieldOptions), nameof<NotificationFieldOptions>(x => x.formatting)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.priorityKey)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.allowAttachments)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.cc)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.ccMode)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bcc)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.bccMode)].join('.'),
			[nameof<NotificationTemplate>(x => x.value),nameof<NotificationTemplateValue>(x => x.extraDataKeys)].join('.'),

			nameof<NotificationTemplate>(x => x.createdAt),
			nameof<NotificationTemplate>(x => x.updatedAt),
			nameof<NotificationTemplate>(x => x.hash),
			nameof<NotificationTemplate>(x => x.isActive),
			nameof<NotificationTemplate>(x => x.belongsToCurrentTenant)
		]
	}

	resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

		const fields = [
			...NotificationTemplateEditorResolver.lookupFields()
		];
		const id = route.paramMap.get('id');

		if (id != null) {
			return this.notificationTemplateService.getSingle(Guid.parse(id), fields).pipe(tap(x => this.breadcrumbService.addIdResolvedValue(x.id?.toString(), x.notificationType.toString())), takeUntil(this._destroyed));
		}
	}
}
