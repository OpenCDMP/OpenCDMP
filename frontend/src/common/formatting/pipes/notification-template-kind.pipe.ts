import { Pipe, PipeTransform } from '@angular/core';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';

@Pipe({ name: 'NotificationTemplateKindFormat' })
export class NotificationTemplateKindPipe implements PipeTransform {
	constructor(private enumUtils: NotificationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toNotificationTemplateKindString(value);
	}
}