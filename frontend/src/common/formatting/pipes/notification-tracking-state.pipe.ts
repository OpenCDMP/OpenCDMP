import { Pipe, PipeTransform } from '@angular/core';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';

@Pipe({
    name: 'NotificationTrackingStateFormat',
    standalone: false
})
export class NotificationTrackingStatePipe implements PipeTransform {
	constructor(private enumUtils: NotificationServiceEnumUtils) { }

	public transform(value): any {
		return this.enumUtils.toNotificationTrackingStateString(value);
	}
}