import { Pipe, PipeTransform } from '@angular/core';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';

@Pipe({ name: 'NotificationTrackingProcessFormat' })
export class NotificationTrackingProcessPipe implements PipeTransform {
	constructor(private enumUtils: NotificationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toNotificationTrackingProcessString(value);
	}
}